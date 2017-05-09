(ns tasks.db
  (:require [cljs.nodejs :as nodejs]
            [tasks.debug :as debug :refer [debug?]]))

(defonce mongoose (nodejs/require "mongoose"))

(defn init []
  (let [uri (or (aget js/process "env" "MONGODB_URI")
                "mongodb://localhost/tasks")]
    (aset mongoose "Promise" (aget js/global "Promise"))
    (doto mongoose
      (.set "debug" debug?)
      (.connect (debug/spy "Connecting to DB" uri)))))
