# kurtosis-jepsen demo
## Instructions:
First go to the docker folder to build the jepsen docker image:
```
cd docker
bin/up --dev
```
Once the docker image is build you can run the demo. Go to the main path in the repo and run kurtosis:
```
cd ..
kurtosis run --enclave=jp .
```
As you can see in the main.star file, this will run three nodes + jepsen controller.
When the kurtosis command finish, you can check the logs of the jepsen controller container. You can use this command:
```
docker logs -f jepsen--1fc41781a8674cd399f27d98eb74f51a
```

You should see something like this output:
```
INFO [2025-07-18 10:31:18,485] jepsen node 172.16.0.6 - jepsen.create-file 172.16.0.6 Created file /tmp/testfile.txt
INFO [2025-07-18 10:31:18,496] jepsen node 172.16.0.5 - jepsen.create-file 172.16.0.5 Created file /tmp/testfile.txt
INFO [2025-07-18 10:31:18,497] jepsen node 172.16.0.4 - jepsen.create-file 172.16.0.4 Created file /tmp/testfile.txt
INFO [2025-07-18 10:31:18,556] jepsen test runner - jepsen.core Run complete, writing
INFO [2025-07-18 10:31:18,617] jepsen test runner - jepsen.core Analyzing...
INFO [2025-07-18 10:31:18,617] jepsen test runner - jepsen.core Analysis complete
INFO [2025-07-18 10:31:18,621] jepsen results - jepsen.store Wrote /test/store/Create file/20250718T103011.539Z/results.edn
INFO [2025-07-18 10:31:18,629] jepsen test runner - jepsen.core {:valid? true}


Everything looks good! ヽ(‘ー`)ノ
```

Now, let's check that the file are in fact in the nodes. You can run this command to connect to the nodes:
```
docker exec -it node-2--d7a46106c6204b4dbd9b2316f24db81e bash
```
You should see this:
```
docker exec -it node-2--d7a46106c6204b4dbd9b2316f24db81e bash
root@5550b48d318d:/# ls tmp/
testfile.txt
```