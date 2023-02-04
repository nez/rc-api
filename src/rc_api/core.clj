(ns rc-api.core
  "API that sets and gets key value pairs"
  (:require [clojure.test :refer [deftest is]]
            [compojure.core :refer [GET defroutes]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.reload :refer [wrap-reload]]))

(def memory-db
  "in memory map with the stored keys and values"
  (atom {}))

(defn memory-db-get
  "get the value corresponding to key `k` in the memory map"
  [k]
  (@memory-db k ""))

(defn memory-db-set
  "store in memory the new `k` `v` pair"
  [k v]
  (swap! memory-db assoc k v))

(defn set-kv
  "set a key value pair, destructuring the `params` map"
  ([params]
   (set-kv (-> params keys first) (-> params vals first)))
  ([k v]
   (memory-db-set k v)
   "ok"))

(defn get-kv
  "get a key value pair, destructuring the `params` map"
  ([params] (memory-db-get (params "key"))))

(defroutes routes
  "API routes"
  (GET "/" [] "Usage: /get?key=k,  /set?k=v")
  (GET "/set" {params :query-params} (set-kv params))
  (GET "/get" {params :query-params} (get-kv params)))

(def app
  "Middlewares for URL parameters and hot reloading"
  (wrap-params (wrap-reload #'routes)))

(defn run
  "Run the API server"
  []
  (run-jetty app
             {:port  4000
              :join? false}))

(defn api-set [k v] (slurp (str "http://localhost:4000/set?" k "=" v)))
(defn api-get [k] (slurp (str "http://localhost:4000/get?key=" k)))

(deftest e2e-tests
  "Test that storage and retrieval endpoints are working properly"
  (is (= "ok" (api-set "test" "true"))
      (= "true" (api-get "test"))))

;; To start the server execute: (run)
;; To run the tests execute: (usage)
;; Code with libraries: https://github.com/nez/rc-api
