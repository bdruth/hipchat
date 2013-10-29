(ns hipchat.core
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]))

;; Fake token for testing
(def ^:dynamic +auth-token+ 
  (atom "2Ites67Z8jHz9afmyjhrlBEqerX9h1DiGnJlwJuh"))

(defn set-auth-token! [token]
  (reset! +auth-token+ token))

(def base-url "https://api.hipchat.com/v2")

(defn request [method url & opts]
  (let [request (http/request 
                  {:url url
                   :query-params {:auth_token @+auth-token+}
                   :method method} opts)
        {:keys [status headers body error] :as resp} @request]
  (if (nil? error)
    (json/parse-string body true)
    {:error error :status status})))

(defn do-request 
  "Makes a http request based on a partial hipchat URL
   i.e /room etc"
  [method partial-url & opts]
  (let [full-url (str base-url partial-url)]
    (request method full-url)))

(def endpoints
  {:rooms {:method :get :endpoint "/room"}})

(defn lookup-params [resource]
 ((juxt :method :endpoint) (get endpoints resource)))

;; Rooms API

(defn rooms [token & opts]
  (let [response (apply do-request 
                   (conj (lookup-params :rooms) 
                     (or opts {})))]
  (->> response :items)))

(defn create-room [name & params]
  )


