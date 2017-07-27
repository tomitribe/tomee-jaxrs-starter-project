# Setup the Raspberry PI Cluster 

## Flash Hypriot Docker Images
You can find the utility to Flash the Hypriot Docker Image here: https://github.com/hypriot/flash

Use the following commands to flash the SD Cards:
- flash --hostname pi-client-01 https://github.com/hypriot/image-builder-rpi/releases/download/v1.4.0/hypriotos-rpi-v1.4.0.img.zip
- flash --hostname pi-client-02 hypriotos-rpi-v1.4.0.img
- flash --hostname pi-client-03 hypriotos-rpi-v1.4.0.img
- flash --hostname pi-client-04 hypriotos-rpi-v1.4.0.img
- flash --hostname pi-grom-server-01 hypriotos-rpi-v1.4.0.img
- flash --hostname pi-grom-server-02 hypriotos-rpi-v1.4.0.img
- flash --hostname pi-thrall-server-01 hypriotos-rpi-v1.4.0.img
- flash --hostname pi-thrall-server-02 hypriotos-rpi-v1.4.0.img
- flash --hostname pi-thrall-load-balancer hypriotos-rpi-v1.4.0.img
- flash --hostname pi-grom-load-balancer hypriotos-rpi-v1.4.0.img
- flash --hostname pi-load-balancer hypriotos-rpi-v1.4.0.img
- flash --hostname pi-thrall-database hypriotos-rpi-v1.4.0.img
- flash --hostname pi-grom-database hypriotos-rpi-v1.4.0.img

## Setup the Router
These instructions are for the Edge Router https://www.ubnt.com/edgemax/edgerouter-x/ :

- Login into the router with ubnt / ubnt.
- Go to izards / WAN + 2LAN2 to set up DHCP Server (Used 10.99.99.1 / 255.255.255.0)
- Go to Services / LAN / View Details / Leases / Map Static IP (Map PI's MAC Addresses to static IPs)
- Go to Configuration / dhcp-server / hostfile-update and set to "enable". (This will register the PI's hostnames in the hosts files and they will be reachable via DNS)
- Set manual hostname for local docker registry (own box - using 10.99.99.11)
    - Login into the router with ssh and ubnt / ubnt and type the commands:
        - configure
        - set system static-host-mapping host-name docker-repo inet 10.99.99.11
        - commit
        - save
        - exit

## Setup Docker Repo Registry
Learn how to setup a local Docker Registry: https://docs.docker.com/registry/deploying/

To generate the certificates:
- openssl req -newkey rsa:4096 -nodes -sha256 -keyout docker-repo.key -x509 -days 365 -out docker-repo.crt

Run the local Docker Registry:
- docker run -d -p 5000:5000 --restart=always --name docker-repo -v `pwd`/certs:/certs -e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/docker-repo.crt -e REGISTRY_HTTP_TLS_KEY=/certs/docker-repo.key registry:2

- The self signed certificates need to be added to the PI's so Docker can downloads images from the local Docker 
Registry. They also need to be added to the local Docker Registry host. For Macs, the easiest way is to add the address 
(docker-repo:5000), in the Mac Docker Daemon / Preferences / Daemon / Insecure Registries.

## Setup PI's
brew create https://sourceforge.net/projects/sshpass/files/sshpass/1.06/sshpass-1.06.tar.gz --force
brew install sshpass
Run the following Ansible playbooks in order:
- ansible-playbook docker-repo/install-repo-certs.yaml -i hosts -f 12
- reboot PI's (docker needs a restart to use the certificate)
- ansible-playbook load-balancerns/load-balancers.yaml -i hosts -f 3
- ansible-playbook install-tomee.yaml -i hosts -f 8
- ansible-playbook databases/install-mysql.yaml -i hosts -f 2

## Add Applications (Client and Server)
- Build client and server with ```mvn clean install docker:build -DpushImage``` (this should build and upload the docker
images to the local registry)
- ansible-playbook servers/servers.yaml -i hosts -f 4
- ansible-playbook clients/clients.yaml -i hosts -f 3

## Useful links:
- https://docker-repo:5000/v2/_catalog (Local Docker Registry Catalog)
- http://pi-load-balancer:5000/stats (HA Proxy Stats)
- http://pi-grom-load-balancer:5000/stats (HA Proxy Stats)
- http://pi-thrall-load-balancer:5000/stats (HA Proxy Stats)

## Setup Metrics
- To run Metrics, use Docker Compose to start an ElasticSearch, Logstash, Kibana environment.
- Execute docker-compose up elk. Then you can just stop / start the container.
- Add the metrics.json into ElasticSearch: curl http://pi-elastic-01:9200/_template/tribe_template -d @metrics.json
- Import the dashboards. Use the file import.json and go to Kibana / Settings / Objects / Import
