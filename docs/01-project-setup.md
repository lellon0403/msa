# 01. 프로젝트 초기 구성

## 목표

`msa/microservices/` 아래에 독립된 Spring Boot 프로젝트 4개를 구성합니다.
교재에 제시된 `product-service` 생성 명령은 프로젝트 하나만 생성합니다.
전체 구조를 구성하려면 나머지 서비스도 각각 생성해야 합니다.

## 서비스와 패키지

| 서비스 | 패키지 / groupId |
|---|---|
| product-service | se.magnus.microservices.core.product |
| recommendation-service | se.magnus.microservices.core.recommendation |
| review-service | se.magnus.microservices.core.review |
| product-composite-service | se.magnus.microservices.composite.product |

## 설정 기록

| 항목 | 교재 예시 | 현재 실습 |
|---|---|---|
| Spring Boot | 3.5.0 | 4.0.8 |
| Java | 24 | 21 |
| 프로젝트 형식 | Gradle | Gradle |
| 패키징 | jar | jar |
| 버전 | 1.0.0-SNAPSHOT | 1.0.0-SNAPSHOT |
| 의존성 | actuator, webflux | actuator, webflux |

현재 버전은 각 서비스의 `build.gradle`에서 확인한 설정입니다. 실제 빌드 및 테스트 성공 여부는 별도 검증이 필요합니다.

## 재현 방법

저장소 루트에서 실행합니다.

```bash
bash scripts/create-services.sh
find microservices -name build.gradle
find microservices/product-service -type f
```

스크립트는 자신의 위치를 기준으로 `microservices/`를 찾으며, 어느 작업 디렉터리에서 호출하든 같은 위치에 생성합니다.
각 서비스 폴더가 이미 존재하면 내용을 변경하지 않고 건너뜁니다.
새로 생성할 때는 Spring CLI와 Spring Initializr에서 해당 버전을 지원해야 합니다.

## 생성 명령의 의미

- `--boot-version`: Spring Boot 버전
- `--type=gradle-project`: Gradle 프로젝트 생성
- `--java-version`: Java 버전
- `--name`: 프로젝트 이름
- `--package-name`: Java 소스의 기본 패키지
- `--groupId`: 빌드에서 사용하는 그룹 식별자
- `--dependencies`: 포함할 의존성
- 마지막 인자: 프로젝트를 생성할 폴더 이름

## 다음 실습 기록

진행한 내용, 실행 명령, 확인 결과, 발생한 오류와 해결 방법을 단계별 문서로 추가합니다.
