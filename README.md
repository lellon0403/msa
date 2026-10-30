# MSA 교재 실습

마이크로서비스 교재를 따라 실습한 코드와 작업 과정을 기록하는 저장소입니다.
`msa` 전체를 하나의 Git 저장소로 관리하며, 서비스는 `microservices/` 아래에 둡니다.

## 폴더 구조

```text
msa/
├── README.md
├── docs/
│   └── 01-project-setup.md
├── scripts/
│   └── create-services.sh
└── microservices/
    ├── product-service/
    ├── recommendation-service/
    ├── review-service/
    └── product-composite-service/
```

각 서비스는 자체 Gradle Wrapper와 소스 코드를 가진 독립된 Spring Boot 프로젝트입니다.

## 실습 환경

- Ubuntu 24.04 (WSL)
- 프로젝트 설정: Spring Boot 4.0.8, Java 21, Gradle
- 의존성: Actuator, Spring WebFlux
- 교재 예시: Spring Boot 3.5.0, Java 24. 버전 차이로 이후 예제에 조정이 필요할 수 있습니다.

## 프로젝트 생성

Spring CLI와 네트워크 연결이 필요합니다. 저장소 루트에서 실행합니다.

```bash
bash scripts/create-services.sh
```

기존 서비스 폴더는 건너뛰며, 없는 서비스만 생성합니다. 생성된 프로젝트는 이미 이 저장소에 포함되어 있으므로 클론 후 다시 생성할 필요는 없습니다.

## 개별 서비스 검증 및 실행

JDK 21이 준비된 환경에서 실행합니다. 최초 실행에는 Gradle과 의존성 다운로드가 필요합니다.

```bash
cd microservices/product-service
./gradlew test
./gradlew bootRun
```

다른 서비스도 해당 폴더에서 같은 명령을 사용합니다. 생성 직후 기본 포트는 모두 8080이므로 동시에 실행하려면 서비스별 포트 설정이 필요합니다.

## 진행 상황

- [x] 서비스 4개 기본 프로젝트 생성
- [x] 프로젝트 생성 스크립트 및 실습 기록 작성
- [ ] 서비스 빌드 및 테스트 검증
- [ ] 교재에 따라 API 구현

자세한 생성 과정은 [프로젝트 초기 구성](docs/01-project-setup.md)에 기록합니다.

## 변경 사항 저장하기

Ubuntu에서 저장소 루트로 이동한 다음 변경 내용을 확인하고 커밋합니다.

```bash
cd ~/msa
git status
git diff
git add README.md docs/ scripts/ microservices/
git commit -m "docs: record study progress"
git push
```

커밋 메시지는 실제 변경 내용에 맞게 수정합니다. 비밀번호, 토큰, 개인 환경 파일은 커밋하지 않습니다.
