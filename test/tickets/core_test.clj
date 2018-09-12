(ns tickets.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :as json]
            [tickets.core :refer [app]]))

(defn parse-body [{:keys [body] :as response}]
  (assoc response :body (json/parse-string (slurp body) true)))

(def default-headers {"Content-Type" "application/json; charset=utf-8"})

(def mock-ticket {:id 1
                  :subject "New Ticket"
                  :body "This is a new ticket, yet"
                  :status {:id 1
                           :name "New"}})

(parse-body (app (mock/request :get "/api/v1/not-found")))

(deftest test-app
  (testing "Main router"
    (let [response (parse-body (app (mock/request :get "/api/v1")))
          expected {:status 200
                    :headers default-headers
                    :body {:message "Hello World"}}]
      (is (= expected response))))

  (testing "Not found"
    (let [response (parse-body (app (mock/request :get "/api/v1/not-found")))
          expected {:status 404
                    :headers default-headers
                    :body {:message "Not Found"}}]
      (is (= expected response))))

  (testing "List tickets"
    (let [response (parse-body (app (mock/request :get "/api/v1/tickets")))
          expected {:status 200
                    :headers default-headers
                    :body {:results [mock-ticket]
                           :page 1
                           :per_page 10
                           :total 1
                           :total_page 1}}]
      (is (= expected response))))

  (testing "Get a ticket"
    (let [response (parse-body (app (mock/request :get "/api/v1/tickets/1")))
          expected {:status 200
                    :headers default-headers
                    :body {:results mock-ticket}}]
      (is (= expected response))))

  (testing "Create a ticket"
    (let [response (parse-body (app (-> (mock/request :post "/api/v1/tickets")
                                        (mock/json-body (select-keys
                                                         mock-ticket
                                                         [:subject :body])))))
          expected {:status 201
                    :headers (conj default-headers {"Location" "/api/v1/tickets/1"})
                    :body {:results mock-ticket}}]
      (is (= expected response)))))
