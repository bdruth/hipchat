(ns hipchat.core
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]))

;; Fake token for testing

(def fake-token "2Ites67Z8jHz9afmyjhrlBEqerX9h1DiGnJlwJuh")

(def ^:dynamic +auth-token+ 
  (atom nil))

(def http-debug "http://requestb.in/1awoam81")

(defn set-auth-token! 
  "Authorize!"
  [token]
  (reset! +auth-token+ token))

(defmacro with-token [token & body]
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
    (with-meta
      (json/parse-string body true)
        base-request)
    {:request base-request 
     :error error 
     :status status})))

(defn do-request 
  "Makes a http request based on a partial hipchat URL
   i.e /room etc"
  [method partial-url & opts]
  (print opts)
  (let [full-url (str base-url partial-url)]
    (request method full-url (into {} opts))))

(def endpoints
  {:rooms {:list {:method :get :endpoint "/room"}
           :create {:method :post :endpoint "/room"}}
   :users {:list {:method :get :endpoint "/user"}}})

(defn lookup-params [resource action]
 ((juxt :method :endpoint) (get-in endpoints [resource action])))

(defn fake-request 
  "Makes a HTTP request to RequestBin to debug any outgoing requests"
  [method & opts]
  (request method http-debug (into {} opts)))

;; Rooms API

(defmacro bind-response-meta 
  "Wraps a response and attaches meta-data
   for debugging"
  [resp & body]
  `(with-meta (do ~@body) 
     (meta ~resp)))

(defn resource-request 
  "Generalized abstraction for typical REST type HTTP requests"
  [resource action & opts]
  (let [response (apply do-request 
                   (conj (lookup-params resource action)
                     (or opts {})))]
  (bind-response-meta response
    (->> response :items))))

(defn rooms 
  "Returns a list of all hipchat rooms"
  [& opts]
  (resource-request :rooms :list))

(defn create-room 
  "Create a new hipchat room"
  [name & params]
  (let [response (apply do-request
                   (conj (lookup-params :rooms :create) 
                     {:name name}))]
    response))

;; Users API

(defn users [& opts]
  (resource-request :users :list))

;; Messages

(defn send-message-to-room [room-id message & opts]
  )

