(ns hipchat.core-test
  (:use clojure.test
        hipchat.core))

(deftest lookup-params-test
  (testing "Should find correct params"
    (is (= (lookup-params :rooms :list) "/room"))))

(deftest replace-id-resource-test
  (testing "should do a string replacement correctly"
    (let [result (replace-id-resource :users :show 10)]
      (is (= result "/user/10")))))
