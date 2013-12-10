;; ******************************************************
;;
;; Hipchat API utilities
;;
;; ******************************************************

(ns hipchat.core
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]))

;; Fake token for testing
(def fake-token "2Ites67Z8jHz9afmyjhrlBEqerX9h1DiGnJlwJuh")

(def ^:dynamic +auth-token+
  (atom nil))

(defn set-auth-token!
  "Authorize!"
  [token]
  (reset! +auth-token+ token))

(defmacro with-token
  [token & body]
  `(binding [+auth-token+ ~token]
     (do ~@body)))

(def base-url "https://api.hipchat.com/v2")

(defn request
  "The core HTTP request function
   which accepts an optional hash of JSON body params"
  [method url & opts]
  (assert ((complement nil?) @+auth-token+))
  (let [f-params (into {} opts)
        auth-params {:auth_token @+auth-token+}
        base-request {:url url
                      :body (json/generate-string f-params)
                      :headers {"Content-Type" "application/json; charset=utf-8"}
                      :query-params auth-params
                      :method method}
        request (http/request base-request nil)
        {:keys [status headers body error] :as resp} @request]
  (if (nil? error)
    (with-meta {:status status
                :body (json/parse-string body true)}
      {:method method})
    {:request base-request
     :error error
     :status status})))

(defn do-request
  "Makes a http request based on a partial hipchat URL
   i.e /room etc"
  [method partial-url & opts]
  (let [full-url (str base-url partial-url)]
    (request method full-url (into {} opts))))

(defn fake-request
  "Makes a HTTP request to RequestBin to debug any outgoing requests"
  [method & opts]
  (let [http-debug-url "http://requestb.in/1awoam81"]
    (request method http-debug-url (into {} opts))))

(def endpoints
  { :capabilities
      {:list {:method :get :endpoint "/capabilities"}}
    :emoticons
      {:list {:method :get :endpoint "/emoticon"}}
    :messages
       {:get {:method :get :endpoint "/rooms/message"}
        :create {:method :post :endpoint "/rooms/message"}}
    :rooms
      {:list {:method :get :endpoint "/room"}
       :show {:method :get :endpoint "/room/:id"}
       :create {:method :post :endpoint "/room"}}
    :users
      {:list {:method :get :endpoint "/user"}
       :show {:method :get :endpoint "/user/:id"}}})

(defn lookup-params [resource action]
 ((juxt :method :endpoint)
   (get-in endpoints [resource action])))

(defmacro bind-response-meta
  "Wraps a response and attaches meta-data
   for debugging"
  [resp & body]
  `(with-meta (do ~@body)
     (meta ~resp)))

(defn is-id-action
  "Does a given action require id substitution?"
  [endpoint]
  (boolean (re-find #"/:id" endpoint)))

(defn replace-id-resource
  "Update a resource action with correct id"
  [resource action id]
  (let [[r a] (lookup-params resource action)]
    [r (clojure.string/replace-first a #":id" (str id))]))

(defn resource-request
  "Generalized abstraction for typical REST type HTTP requests
   will do substitution on URL ids i.e /resource/:id if a map
   with an ID key is passed in as opts i.e {:id 10}"
  [resource action & opts]
  (let [opt-map (into {} opts)
        [meth endp] (lookup-params resource action)
        resource-vec (if (contains? opt-map :id)
                       (replace-id-resource resource action (:id opt-map))
                       [meth endp])
        response (apply do-request
                   (conj resource-vec opt-map))]
   (bind-response-meta response
     response)))

(defn capabilities []
  (resource-request :capabilities :list))

(defn with-items [resource action]
  (->> (resource-request resource action) :items))

;; Emoticons
;; ******************************************************

(defn emoticons
  "API essentials : )"
  []
  (with-items :emoticons :list))

(def emoticon-names
  (comp (partial map :shortcut) emoticons))

;; Users
;; ******************************************************

(defn users [& opts]
  (->> (resource-request :users :list) :items))

(defn user [id]
  (resource-request :users :show {:id id}))

;; Rooms
;; ******************************************************

(defn rooms
  "Returns a list of all hipchat rooms"
  [& opts]
  (->> (resource-request :rooms :list) :body :items))

(def room-names
  "Extract a list of room names"
  (let [room-names (partial map :name)]
    (comp room-names rooms)))

(defn create-room
  "Create a new hipchat room"
  [name & params]
  (apply do-request
    (conj (lookup-params :rooms :create)
      {:name name})))

(defn room
  "Get a single room"
  [id-or-name]
  (resource-request :rooms :show {:id id-or-name}))

(defn room-members
  "TODO check this endpoint?"
  [room-identifier]
  (let [room-name (if (keyword? room-identifier)
                    (name room-identifier) room-identifier)]
    (do-request :get
      (format "/room/%s/member" room-identifier))))

(defn message
  "Create a new hipchat room
   room can be either an id or room name
   Optional params
     - color string [yellow, red, green, purple, gray]
     - notify boolean"
  [room message & params]
  (let [room-name (if (keyword? room) (name room) room)
        room-url (format "/room/%s/notification" room-name)]
      (apply (partial do-request :post)
               [room-url (merge-with conj
                           {:message message} (into {} params))])))

(comment
  (with-token (atom fake-token)
    (create-message :forward "Hi there @OwainLewis"
      {:color "red"})))

