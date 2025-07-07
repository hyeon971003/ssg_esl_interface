# Step 1: 의존성 다운로드
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /build

# Copy the Maven project files
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy the source code and package the application, skipping tests for faster builds
COPY src ./src
RUN mvn -B package -DskipTests

# Step 2: 최종 이미지 준비
FROM openjdk:17-slim
WORKDIR /app

# unzip, nano, vi (혹은 vim) 설치
RUN apt-get update && \
    apt-get install -y unzip nano vim && \
    rm -rf /var/lib/apt/lists/*

# 빌드 단계에서 생성된 JAR 파일을 복사합니다.
COPY --from=build /build/target/*.jar app.jar

# 필요한 디렉토리 생성
RUN mkdir -p /app/esl_if/daily
RUN mkdir -p /app/esl_if/rpl
RUN mkdir -p /app/esl_if/log

# Spring 프로파일 설정
ENV SPRING_PROFILES_ACTIVE=dev

# 애플리케이션 실행
CMD ["java", "-jar", "/app/app.jar"]
