(ns hipchat.core
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]))

;; Fake token for testing
(def ^:dynamic +auth-token+ 
  (atom "2Ites67Z8jHz9afmyjhrlBEqerX9h1DiGnJlwJuh"))

(defn set-auth-token! [token]
  (reset! +auth-token+ token))

(defmacro with-token [token & body]
  `(binding [+auth-token+ token]
     (do ~@body)))

(def base-url "https://api.hipchat.com/v2")

(defn request [method url & opts]
  (let [base-request {:url url
                      :form-params (or opts {})
                      :content-type "application/json"
                      :query-params {:auth_token @+auth-token+}
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
  (let [full-url (str base-url partial-url)]
    (request method full-url)))

(def endpoints
  {:rooms {:list {:method :get :endpoint "/room"}
           :create {:method :post :endpoint "/room"}}})

(defn lookup-params [resource action]
 ((juxt :method :endpoint) (get-in endpoints [resource action])))

;; Rooms API

(defmacro bind-response-meta 
  "Wraps a response and attaches meta-data
   for debugging"
  [resp & body]
  `(with-meta (do ~@body) 
     (meta ~resp)))

(defn rooms 
  "Returns a list of all hipchat rooms"
  [& opts]
  (let [response (apply do-request 
                   (conj (lookup-params :rooms :list) 
                     (or opts {})))]
  (bind-response-meta response
    (->> response :items))))

(defn create-room 
  "Create a new hipchat room"
  [name & params]
  (let [response (apply do-request
                   (conj (lookup-params :rooms :create) 
                     {:name name}))]

    response))


