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
  [node path filename]
  (c/exec :mkdir "-p" path)
  (let [file-path (str path "/" filename)]
    (c/exec :touch file-path)
    (info node "Created file" file-path)))

(defn file-exists? [folder filename]
  (let [path (str folder "/" filename)]
    (try
      (c/exec :test "-f" path)
      true
      (catch Exception _
        false))))

(defn client-create-test
  [path filename]
    (reify client/Client
      (open! [this test node]
        (c/on node (create-file-op! node path filename))
        (let [exists? (c/on node (file-exists? path filename))
            fp      (str path "/" filename)]
          (info node "File exists?" exists?)
          (assert exists? (str "File not created: " path "/" filename)))
        this)
      (setup! [this _]
        this)
      (teardown! [this _]
        this)
      (close! [this _]
        this)))

(defn create-file-test
  [nodes path filename]
  (assoc tests/noop-test
          :name      "Create file"
          :os        debian/os
          :nodes     nodes
          :concurrency (count nodes)
          :client    (client-create-test path filename)))

(defn -main [& args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)]
    (println "DEBUG options:" options)
    (cond
      (:help options)
      (do
        (println "Usage: lein run -- --nodes <ip_node_1>,<ip_node_2> --path /tmp/jepsen --filename testfile.txt")
        (println summary)
        (System/exit 0))

      (or (not (:nodes options))
          (not (:path options))
          (not (:filename options)))
      (do
        (println "Missing required arguments!\n")
        (println "Usage: lein run -- --nodes <ip_node_1>,<ip_node_2> --path /tmp/jepsen --filename testfile.txt")
        (println summary)
        (System/exit 1))

      :else
      (jepsen/run!
        (create-file-test
          (:nodes options)
          (:path options)
          (:filename options))))))