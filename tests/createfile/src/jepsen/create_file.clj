(ns jepsen.create-file
  (:require [jepsen.core :as jepsen]
            [jepsen.control :as c]
            [jepsen.control.util :as cu]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :refer :all]
            [jepsen.client :as client]
            [jepsen.tests :as tests]
            [jepsen.os.debian :as debian]))

;; CLI option definitions
(def cli-options
  [
   ["-p" "--path PATH" "Folder path"]
   ["-f" "--filename FILE" "File name"]
   ["-n" "--nodes NODES" "List of nodes"
   :parse-fn #(clojure.string/split % #",")]
   ["-h" "--help" "Show help"]
   ])

(defn create-file-op!
  [path filename]
  (let [file-path (str path "/" filename)]
    (info "Creating file on node at" file-path)
    (c/exec :mkdir "-p" path)
    (c/exec :touch file-path)
    (info "Created file" file-path)))

(defn client-create-test
  [node path filename]
  (println "3 Node value:" node)
  (reify client/Client
    (open! [_ test node]
      (c/on node #(create-file-op! path filename)))
    (invoke! [_ test op] op)
    (teardown! [this test] this)))

(defn create-file-test
  [nodes path filename]
  (println "2 Nodes value:" nodes)
  (println "2 Node types:" (mapv type nodes))
  (assoc tests/noop-test
          :name      "Create file"
          :os        debian/os
          :nodes     nodes
          ;; :username  "ubuntu"
          ;; :ssh       {:private-key-path "/root/.ssh/id_rsa" :strict-host-key-checking false}
          :nemesis   nil
          :client    (fn [node] (client-create-test node path filename))
          :generator (fn [_ _] []) ; No actual operations, only setup
          :model     nil))

(defn -main [& args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)]
    (println "DEBUG options:" options)
    (println "Nodes value:" (:nodes options))
    (println "Node types:" (mapv type (:nodes options)))
    (cond
      (:help options)
      (do
        (println "Usage: lein run -- --nodes node1,node2 --path /tmp/jepsen --filename testfile.txt")
        (println summary)
        (System/exit 0))

      (or (not (:nodes options))
          (not (:path options))
          (not (:filename options)))
      (do
        (println "Missing required arguments!\n")
        (println "Usage: lein run -- --nodes node1,node2 --path /tmp/jepsen --filename testfile.txt")
        (println summary)
        (System/exit 1))

      :else
      (jepsen/run!
        (create-file-test
          (:nodes options)
          (:path options)
          (:filename options))))))