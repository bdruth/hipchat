(ns hipchat.core-test
  (:use clojure.test
        hipchat.core))

(deftest lookup-params-test
  (testing "Should find correct params"
    (is (= (lookup-params :rooms :list) [
      :get "/room"]))))

(deftest replace-id-resource-test
  (testing "should do a string replacement correctly"
    (let [result (replace-id-resource :users :show 10)]
      (is (= result 
        [:get "/user/10"])))))

(deftest get-room-test
   (testing "should get all hipchat rooms"
     (let [response 
             (with-token (atom fake-token)
               (room "api-test"))]
   (is (= (:status response) 200)))))

(deftest get-rooms-test
   (testing "should get all hipchat rooms"
     (let [response 
             (with-token (atom fake-token)
               (rooms))])))

(deftest send-message-test
  (testing "should send a hipchat message"
    (let [response 
           (with-token (atom fake-token)
             (message "api-test" "Hi there"))]
   (is (= (:status response) 204)))))
