🐾 Stray Animal Emergency Response Platform

Bridging the gap between citizens, NGOs, and municipal authorities — because every second counts.


🚨 Problem Statement
Every day, stray animals are involved in accidents on city roads. But the system fails them:

❌ No centralized platform to report animal emergencies
❌ No direct communication channel between citizens, NGOs, and municipal authorities
❌ Manual phone calls and forwarding waste critical response time

The result? Animals die waiting for help that never arrives in time.

✅ Solution
A microservices-based emergency response platform that creates a single digital ecosystem where:

👤 Citizens can instantly report an injured stray animal with photos and location details
📍 The backend automatically identifies nearby NGOs and municipal bodies based on city/location
🔔 Real-time notifications are dispatched instantly via event-driven messaging
🏥 NGOs and municipal teams can acknowledge and respond without delay


🧠 System Architecture
mermaidgraph LR
    User -->|Request| GW[API Gateway]
    GW --> Auth[Auth Service]
    GW --> CS[Complaint Service]
    GW --> NGO[NGO Service]
    GW --> MUN[Municipal Service]
    CS -->|Publish Event| RMQ[RabbitMQ]
    RMQ -->|Consume| NS[Notification Service]
    NS -->|Alert| NGO
    NS -->|Alert| MUN
PrincipleImplementationService DesignMicroservices — loosely coupled, independently deployableCommunicationEvent-driven via RabbitMQDeploymentFully DockerizedSecurityOAuth2 + Spring Security

🛠️ Tech Stack
CategoryTechnologyLanguageJava 17FrameworkSpring Boot, Spring CloudSecuritySpring Security, OAuth2, JWTDatabaseMongoDBMessagingRabbitMQDevOpsDocker, Docker ComposeService DiscoveryEurekaCI/CDGitHub Actions

🔐 Security

OAuth2-based Authentication
JWT Token-based Authorization
Role-Based Access Control (RBAC):

RolePermissionsUSERSubmit emergency reportsNGOReceive notifications, acknowledge complaintsMUNICIPALMunicipal authority dashboard accessADMINFull system control

🧩 Microservices Breakdown
1️⃣ API Gateway Service
Central entry point for all incoming requests. Handles routing, authentication, and security enforcement.
2️⃣ Auth Service
Manages user registration and login. Issues OAuth2-compliant JWT tokens for secure access.
3️⃣ Complaint Service
Accepts accident reports from citizens. Stores animal details, uploaded images, and location data.
4️⃣ NGO Service
Manages NGO profiles and filters relevant organizations based on city and location.
5️⃣ Municipal Service
Maintains municipal authority data with city-wise mapping for targeted notification routing.
6️⃣ Notification Service
Listens to RabbitMQ events and dispatches real-time alerts to the appropriate NGOs and municipal teams.

📍 Location-Based Matching Logic
When a user submits a complaint, the system:

📌 Extracts the city/location from the complaint
🔍 Fetches nearby NGOs and municipal bodies
📨 Publishes an event to RabbitMQ
🔔 Notification Service triggers targeted alerts

The result: minimum response time, maximum impact.

🚀 Getting Started
Prerequisites

Docker & Docker Compose
Java 17
MongoDB

Run Locally
bash# Clone the repository
git clone https://github.com/your-username/your-repo-name.git

# Navigate into the project directory
cd your-repo-name

# Start all services
docker-compose up --build
All microservices will spin up automatically. 🎉

🐳 Docker Setup
Each microservice runs in its own isolated container, providing:

✅ Independent scaling per service
✅ Consistent environments across dev, staging, and production
✅ AWS-ready cloud deployment out of the box


📦 Roadmap

 📱 Mobile App (Android & iOS)
 🗺️ Live Google Maps Tracking
 📊 Admin Analytics Dashboard
 🔔 SMS / WhatsApp Alert Integration
 🤖 AI-based Injury Severity Detection from Images


💡 Why This Project Matters

"The best use of technology is when it saves a life."


🐶🐱 Directly saves stray animal lives
⏱️ Drastically reduces emergency response time
🏛️ Enables transparent coordination between NGOs and city authorities
🌍 Real-world social impact built on modern software engineering


👨‍💻 Author
Developed by: Manas Rastogi
Role: Backend / Java Microservices Developer
Tech Focus: Spring Boot • Microservices • Docker • Cloud • GitHub Actions CI/CD

🤝 Contributing
Contributions are welcome! If you find a bug or want to suggest a feature:

Fork the repository
Create your feature branch (git checkout -b feature/AmazingFeature)
Commit your changes (git commit -m 'Add some AmazingFeature')
Push to the branch (git push origin feature/AmazingFeature)
Open a Pull Request


⭐ Support
If this project resonates with you, please give it a ⭐ star and share it.

"Together, we can use technology to make cities more compassionate." ❤️
