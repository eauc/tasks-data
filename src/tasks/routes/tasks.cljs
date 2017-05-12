(ns tasks.routes.tasks
  (:require [cljs.nodejs :as nodejs]
            [tasks.auth :refer [auth perms]]
            [tasks.models.tasks-list :refer [TasksList]]
            [tasks.debug :as debug]))

(defn- mapId [x]
  (-> x
      (assoc :id (:_id x))
      (dissoc :_id)))

(defn- queryResult->tasksList [result]
  (-> (.toObject result)
      (js->clj :keywordize-keys true)
      (select-keys [:_id :name :tasks :updatedAt :user])
      (update :tasks #(mapv mapId %))
      (mapId)))

(defn- queryResult->tasksListResource [result]
  (let [tasks-list (queryResult->tasksList result)]
    {:link (str "/tasks/" (:id tasks-list))
     :tasksList tasks-list}))

(defn- tasksListResponse [res result {:keys [status]}]
  (let [resource (queryResult->tasksListResource result)]
    (-> res
        (.status (or status 200))
        (.json (clj->js resource)))))

(defn- tasksListsRequest [query res next]
  (-> TasksList
      (.find query)
      (.sort "name")
      (.select "_id user name")
      (.exec)
      (.then
       (fn [result]
         (let [tasksLists (mapv queryResult->tasksListResource (js->clj result))]
           (.json res (clj->js {:link "/tasks" :tasksLists tasksLists})))))))

(defn tasksListsRoute [req res next]
  (tasksListsRequest #js {:user (aget req "user" "email")} res next))

(defn tasksListsAdminRoute [req res next]
  (tasksListsRequest #js {} res next))

(defn tasksCreateRoute [req res next]
  (aset req
        "body" "user"
        (debug/spy "email" (aget req "user" "email")))
  (-> req
      (aget "body")
      (TasksList)
      (.save)
      (.then #(tasksListResponse res % {:status 201}))))

(defn- whenFound [res on-found]
  (fn [result]
    (let [tasks-list (first result)]
      (if tasks-list
        (on-found tasks-list)
        (-> res (.status 404) (.json #js {:message "not found"}))))))

(defn tasksByIdRoute [req res next]
  (-> TasksList
      (.find #js {:_id (aget req "params" "id")
                  :user (aget req "user" "email")})
      (.exec)
      (.then (whenFound res #(tasksListResponse res % {})))))

(defn- updateTasksList [result params]
  (.save (doto result
           (aset "name" (or (aget params "name")
                            (aget result "name")))
           (aset "tasks" (or (aget params "tasks")
                             (aget result "tasks"))))))

(defn tasksUpdateRoute [req res next]
  (-> TasksList
      (.find #js {:_id (aget req "params" "id")
                  :user (aget req "user" "email")})
      (.exec)
      (.then (whenFound res #(updateTasksList % (aget req "body"))))
      (.then #(tasksListResponse res % {}))))

(defn tasksRemoveRoute [req res next]
  (-> TasksList
      (.remove #js {:_id (aget req "params" "id")
                    :user (aget req "user" "email")})
      (.then #(-> res (.status 200) (.json %)))))

(defn tasksRemoveAdminRoute [req res next]
  (-> TasksList
      (.remove #js {:_id (aget req "params" "id")})
      (.then #(-> res (.status 200) (.json %)))))

(defn- catchErrorResponse [route]
  (fn [req res next]
    (.catch (route req res next) next)))

(defn router [app]
  (doto app
    (.get "/tasks/mine"
          auth (.check perms "user:default")
          (catchErrorResponse tasksListsRoute))
    (.post "/tasks/mine"
           auth (.check perms "user:default")
           (catchErrorResponse tasksCreateRoute))
    (.get "/tasks/mine/:id"
          auth (.check perms "user:default")
          (catchErrorResponse tasksByIdRoute))
    (.put "/tasks/mine/:id"
          auth (.check perms "user:default")
          (catchErrorResponse tasksUpdateRoute))
    (.delete "/tasks/mine/:id"
             auth (.check perms "user:default")
             (catchErrorResponse tasksRemoveRoute))
    (.get "/tasks"
          auth (.check perms "admin")
          (catchErrorResponse tasksListsAdminRoute))
    (.delete "/tasks/:id"
             auth (.check perms "admin")
             (catchErrorResponse tasksRemoveAdminRoute))))
