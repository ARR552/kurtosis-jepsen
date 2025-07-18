def run(plan):
    db_ips = []
    node_ssh_artifact = plan.upload_files(
        name="node-ssh",
        src="ssh-keys/nodes/"
    )
    for i in range(3):
        svc = plan.add_service(
            name="node-" + str(i),
            config=ServiceConfig(
                image="ubuntu:22.04",
                files={"/home/ubuntu/.ssh/": node_ssh_artifact},
                ports={"ssh": PortSpec(22)},  # expose SSH port if needed
                cmd=[
                    "bash", "-c",
                    "apt-get update && apt-get install -y openssh-server sudo && " +
                    "useradd -m -s /bin/bash ubuntu && echo 'ubuntu:ubuntu' | chpasswd && " +
                    "mkdir /var/run/sshd && " +
                    "echo 'PermitRootLogin yes' >> /etc/ssh/sshd_config && " +
                    "echo 'PasswordAuthentication yes' >> /etc/ssh/sshd_config && " +
                    "echo 'PubkeyAuthentication yes' >> /etc/ssh/sshd_config && " +
                    "echo 'AuthorizedKeysFile .ssh/authorized_keys' >> /etc/ssh/sshd_config && " +
                    "chmod 700 /home/ubuntu/.ssh &&" +
                    "chmod 600 /home/ubuntu/.ssh/authorized_keys && " +
                    "chown ubuntu:ubuntu /home/ubuntu/.ssh /home/ubuntu/.ssh/authorized_keys && " +
                    "echo 'root:root' | chpasswd && " +
                    "/usr/sbin/sshd -D"
                ],
            )
        )
        db_ips.append(svc.ip_address)

    jepsen_test_artifact = plan.upload_files(
        name="jepsen-test",
        src="tests/createfile/"
    )
    jepsen_ssh_artifact = plan.upload_files(
        name="jepsen-ssh",
        src="ssh-keys/controller/"
    )
    # Jepsen controller
    jepsen = plan.add_service(
        name="jepsen",
        config=ServiceConfig(
            image="jepsen_control:latest",
            files={"/test/": jepsen_test_artifact, "/root/.ssh/": jepsen_ssh_artifact},
            cmd=[
                # "sleep", "3600"
                "lein", "run",
                "--",
                "--nodes", ",".join(db_ips),
                "--path", "/tmp",
                "--filename", "testfile.txt"
            ],
        )
    )

    # Add each node's key to known_hosts in the Jepsen container
    for ip in db_ips:
        plan.exec(
            service_name="jepsen",
            recipe=ExecRecipe(
                command=[
                    "bash", "-c",
                    "ssh-keyscan -t ed25519 " + ip + " >> /root/.ssh/known_hosts"
                ]
            )
        )

    # (optional) wait for Jepsen to finish & export a results tarball
    # plan.exec(service_name="jepsen", recipe=ExecRecipe(command=["sleep", "3600"]))
