# 7주차 AWS EC2 및 Docker 실습

## 1. 실습 환경

- **AWS:** Free Plan, EC2 `t3.micro` (시드니 리전)
- **OS:** Ubuntu Server 24.04 LTS
- **스토리지:** 8 GiB gp3
- **웹 서버:** nginx (Docker 컨테이너)

## 2. 실습 과정

1. AWS 계정을 생성하고 MFA 및 예산 알림을 설정했다.
2. EC2 인스턴스를 생성하고 키 페어(`.pem`)를 다운로드했다.
3. 보안 그룹에서 SSH(22)는 내 IP만, HTTP(80)는 외부 접속을 허용했다.
4. PowerShell에서 SSH로 EC2에 접속했다.

   ```powershell
   ssh -i "week7-key.pem" ubuntu@<PUBLIC_IP>
   ```

5. Ubuntu에 Docker를 설치하고 실행했다.

   ```bash
   sudo apt update
   sudo apt install -y docker.io
   sudo systemctl enable --now docker
   ```

6. nginx 컨테이너를 실행하고 상태를 확인했다.

   ```bash
   sudo docker run -d -p 80:80 --name nginx-server nginx
   sudo docker ps
   ```

7. 브라우저에서 `http://<PUBLIC_IP>`에 접속하여 **Welcome to nginx!** 화면을 확인했다.

### 실행 결과

> 이 위치에 nginx 접속 성공 스크린샷을 첨부한다.

![nginx 실행 결과](\nginx-success.png)

## 3. 개념 정리

| 서비스 | 역할 |
|---|---|
| **EC2** | 클라우드에서 가상 서버를 제공한다. |
| **RDS** | 관계형 데이터베이스의 생성과 운영을 관리한다. |
| **S3** | 파일 등의 데이터를 객체 형태로 저장한다. |

- **Docker Image:** 애플리케이션 실행에 필요한 파일과 설정을 담은 읽기 전용 템플릿이다.
- **Docker Container:** 이미지를 기반으로 생성되어 실행되는 격리된 환경이다.
- **`8080:8080` 포트 매핑:** 호스트의 8080번 포트로 들어온 요청을 컨테이너의 8080번 포트로 전달한다. 이번 실습에서는 `80:80`을 사용했다.

### 배포 실패 시 확인 항목

- EC2 인스턴스 실행 상태 및 퍼블릭 IP
- 보안 그룹의 HTTP(80), SSH(22) 인바운드 규칙
- Docker 실행 상태: `sudo systemctl status docker`
- 컨테이너 실행 상태: `sudo docker ps -a`
- 컨테이너 로그: `sudo docker logs nginx-server`
- 포트 매핑 및 접속 URL 확인

## 4. 결론

AWS EC2에 Docker를 설치하고 nginx 컨테이너를 실행하여, 퍼블릭 IP를 통한 웹 서버 접속에 성공했다.
