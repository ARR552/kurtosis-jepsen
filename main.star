def run(plan):
    db_ips = []
    for i in range(3):
        svc = plan.add_service(
            name="node-" + str(i),
            config=ServiceConfig(
                image="ubuntu:22.04",
                ports={"ssh": PortSpec(22)},  # expose SSH port if needed
                cmd=[
                    "bash", "-c",
                    "apt-get update && apt-get install -y openssh-server sudo && " +
                    "useradd -m -s /bin/bash ubuntu && echo 'ubuntu:ubuntu' | chpasswd && " +
                    "mkdir /var/run/sshd && " +
                    "echo 'PermitRootLogin yes' >> /etc/ssh/sshd_config && " +
                    "echo 'PasswordAuthentication yes' >> /etc/ssh/sshd_config && " +
                    "/usr/sbin/sshd -D"
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
                "sleep", "3600"
                # "lein", "run",
                # "--",
                # "--nodes", ",".join(db_ips),
                # "--path", "/",
                # "--filename", "testfile.txt"
            ],
        )
    )

    # (optional) wait for Jepsen to finish & export a results tarball
    # plan.exec(service_name="jepsen", recipe=ExecRecipe(command=["sleep", "3600"]))
