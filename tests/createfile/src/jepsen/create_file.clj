(ns jepsen.create-file
  (:require [jepsen.core :as jepsen]
            [jepsen.control :as c]
            [jepsen.control.util :as cu]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :refer :all]
            [jepsen.client :as client]))

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
  (let [path (str path "/" filename)]
    (info "Creating file on node at" path)
    (c/exec :mkdir "-p" path)
    (c/exec :touch path)
    (info "Created file" path)))

(defn create-file-test
  [nodes path filename]
  {:name      "Create file"
   :os        :debian
   :nodes     nodes
   :username  "ubuntu"
   :nemesis   nil
   :client    (reify client/Client
                (setup! [this test]
                  (doseq [node nodes]
                    (c/on node (create-file-op! path filename)))
                  this)
                (invoke! [this test op] op)
                (teardown! [this test] this))
   :generator (fn [_ _] []) ; No actual operations, only setup
   :model     nil})

(defn -main [& args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)]
    (println "DEBUG options:" options)
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