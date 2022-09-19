export LD_LIBRARY_PATH=/opt/occlum/toolchains/gcc/x86_64-linux-musl/lib
export JAVA_HOME=/opt/occlum/toolchains/jvm/java-11-alibaba-dragonwell
./mvnw clean
./mvnw install -Dmaven.test.skip=true
