(ns tasks.libs
  (:require [cljs.nodejs :as nodejs]))

(defonce body-parser (nodejs/require "body-parser"))
(defonce compression (nodejs/require "compression"))
(defonce cors (nodejs/require "cors"))
(defonce helmet (nodejs/require "helmet"))

(defn json-body->clj [req res next]
  (aset req "body" (-> req
                       (aget "body")
                       (js->clj :keywordize-keys true)
                       (dissoc :_id :id :__v :createdAt :updatedAt)
                       (clj->js)))
  (next))

(defn init [app]
  (doto app
    (.set "json spaces" 2)
    (.use (compression))
    (.use (cors (clj->js {:methods ["DELETE" "GET" "POST" "PUT"]
                          :allowedHeaders ["Authorization" "Content-Type"]})))
    (.use (helmet))
    (.use (.json body-parser))
    (.use json-body->clj)))
