rm ./target/*.jar
mvn package
docker build . -t fan9704/iptables_controller_backend
docekr push fan9704/iptables_controler_backend