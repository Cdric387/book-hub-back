# BookHub — Backend Spring Boot

API REST pour la plateforme de gestion de bibliothèque communautaire.

## Prérequis

- Java 17+
- SQL Server 2019+ (voir ci-dessous pour Docker)
- IntelliJ IDEA

## Lancer SQL Server avec Docker (recommandé)

```bash
docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=BookHub2024!" \
  -p 1433:1433 --name bookhub-sql \
  -d mcr.microsoft.com/mssql/server:2022-latest
```

Puis créer la base :
```bash
docker exec -it bookhub-sql /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P "BookHub2024!" -No \
  -Q "CREATE DATABASE bookhub"
```

## Lancer le projet

1. Cloner le repo
2. Ouvrir dans IntelliJ
3. Vérifier `src/main/resources/application.yml` (port SQL Server, password)
4. Run `BookHubApplication.java`

## Accès

- API : http://localhost:8080
- Swagger : http://localhost:8080/swagger-ui.html

## Structure

```
src/main/java/com/bookhub/
├── entity/          Entités JPA (User, Book, Loan, Reservation, Rating)
├── repository/      Interfaces Spring Data JPA
├── dto/             Objets de transfert (Request/Response)
├── service/         Logique métier
├── controller/      Contrôleurs REST
├── security/        JWT (JwtService, JwtAuthFilter)
├── config/          SecurityConfig, SwaggerConfig
└── exception/       Gestion globale des erreurs
```

## Endpoints principaux

| Méthode | Route | Accès |
|---------|-------|-------|
| POST | /api/auth/register | Public |
| POST | /api/auth/login | Public |
| GET | /api/books | Public |
| GET | /api/books/{id} | Public |
| GET | /api/books/search?q= | Public |
| POST | /api/books | LIBRARIAN/ADMIN |
| PUT | /api/books/{id} | LIBRARIAN/ADMIN |
| DELETE | /api/books/{id} | ADMIN |

## Rôles

- `USER` : lecture, emprunt, réservation, notation
- `LIBRARIAN` : + gestion catalogue, retours
- `ADMIN` : accès complet
