(ns tasks.libs
  (:require [cljs.nodejs :as nodejs]))

(defonce body-parser (nodejs/require "body-parser"))
(defonce compression (nodejs/require "compression"))
(defonce cors (nodejs/require "cors"))
(defonce helmet (nodejs/require "helmet"))

(defn init [app]
  (doto app
    (.set "json spaces" 2)
    (.use (compression))
    (.use (cors (clj->js {:methods ["DELETE" "GET" "POST" "PUT"]
                          :allowedHeaders ["Authorization" "Content-Type"]})))
    (.use (helmet))
    (.use (.json body-parser))))
