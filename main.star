def run(plan):
    db_ips = []
    for i in range(3):
        svc = plan.add_service(
            name="db-" + str(i),
            config=ServiceConfig(
                image="cockroachdb/cockroach:v24.1.2",
                ports={"sql": PortSpec(26257), "http": PortSpec(8080)},
                cmd=[
                    "start", "--insecure",
                    "--join=db-0:26257"
                ],
            )
        )
        db_ips.append(svc.ip_address)

    jepsen_test_artifact = plan.upload_files(
        name="jepsen-test",
        src="tests/createfile/"
    )
    # Jepsen controller
    jepsen = plan.add_service(
        name="jepsen",
        config=ServiceConfig(
            image="jepsen_control:latest",
            files={"/test/": jepsen_test_artifact},
            # entrypoint = ["/test"],
            cmd=[
                # "sleep", "3600"
                "lein", "run",
                "--",
                "--nodes", ",".join(db_ips),
                "--path", "/",
                "--filename", "testfile.txt"
            ],
        )
    )

    # (optional) wait for Jepsen to finish & export a results tarball
    # plan.exec(service_name="jepsen", recipe=ExecRecipe(command=["sleep", "3600"]))
