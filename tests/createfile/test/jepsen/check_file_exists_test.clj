(ns jepsen.check-file-exists-test
  (:require [jepsen.core :as jepsen]
            [jepsen.control :as c]
            [clojure.tools.logging :refer :all]
            [jepsen.client :as client]))

(defn file-exists? [folder filename]
  (let [path (str folder "/" filename)]
    (try
      (c/exec :test "-f" path)
      true
      (catch Exception _
        false))))

(defn file-exists-test
  [nodes folder filename]
  (jepsen/test
    {:name      "File existence validation"
     :os        :debian
     :nodes     nodes
     :nemesis   nil
     :client    (reify client/Client
                  (setup! [this test _]
                    (doseq [node nodes]
                      (c/on node
                        (c/exec :mkdir "-p" folder)
                        (c/exec :touch (str folder "/" filename))))
                    this)
                  (invoke! [this test op]
                    (let [results
                          (for [node nodes]
                            (c/on node
                              {:node node
                               :exists (file-exists? folder filename)}))]
                      (assoc op
                             :results results
                             :type (if (every? :exists results) :ok :fail))))
                  (teardown! [this test] this))
     :generator (fn [_ _]
                  [{:type :check}]) ; Only check once
     :model     nil}))

;; Example usage (uncomment and set real nodes):
#_(jepsen/run! (file-exists-test ["node1" "node2"] "/tmp/jepsen" "testfile.txt"))