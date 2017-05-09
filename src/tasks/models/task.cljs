(ns tasks.models.task
  (:require [cljs.nodejs :as nodejs]))

(defonce mongoose (nodejs/require "mongoose"))
(defonce Schema (aget mongoose "Schema"))
(defonce Types (aget Schema "Types"))

(def taskSchema
  (Schema. (clj->js {:id {:type (aget Types "String")
                          :minLength 1
                          :required true
                          :trim true}
                     :title {:type (aget Types "String")
                             :minLength 1
                             :required true
                             :trim true}
                     :body {:type (aget Types "String")
                            :required true
                            :trim true}
                     :done {:type (aget Types "Boolean")
                            :required true}})))

(def Task
  (.model mongoose "Task" taskSchema))
