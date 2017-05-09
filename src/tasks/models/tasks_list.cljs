(ns tasks.models.tasks-list
  (:require [cljs.nodejs :as nodejs]
            [tasks.models.task :refer [taskSchema]]))

(defonce mongoose (nodejs/require "mongoose"))
(defonce Schema (aget mongoose "Schema"))
(defonce Types (aget Schema "Types"))

(def tasksListSchema
  (Schema. (clj->js {:user {:type (aget Types "String")
                            :minLength 1
                            :required true
                            :trim true}
                     :name {:type (aget Types "String")
                            :minLength 1
                            :required true
                            :trim true}
                     :tasks [taskSchema]})
           #js {:timestamps true}))

(def TasksList
  (.model mongoose "TasksList" tasksListSchema))
