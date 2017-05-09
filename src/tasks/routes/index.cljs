(ns tasks.routes.index
  (:require [cljs.nodejs :as nodejs]
            [tasks.debug :as debug]))

(defn indexRoute [req res]
  (.json res (clj->js
              {:status "Tasks Data API"
               :version "1.0.0"
               :tasks {:link "/tasks"
                       :mine "/tasks/mine"}})))

(defn router [app]
  (doto app
    (.get "/" indexRoute)))

