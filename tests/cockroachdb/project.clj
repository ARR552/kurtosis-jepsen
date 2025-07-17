(defproject cockroachdb "0.1.0"
  :description "Jepsen testing for CockroachDB"
  :url "http://cockroachlabs.com/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [jepsen "0.3.9"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [circleci/clj-yaml "0.5.5"]
                 [org.postgresql/postgresql "9.4.1211"]
                 [org.flatland/ordered "1.5.9"]]
  :jvm-opts ["-Xmx12g"
             "-XX:MaxInlineLevel=32"
             "-XX:MaxRecursiveInlineLevel=2"
             "-server"]
  :main jepsen.cockroach.runner
  :aot [jepsen.cockroach.runner
        clojure.tools.logging.impl])
