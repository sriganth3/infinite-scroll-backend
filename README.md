# Infinite Scroll Backend App using SpringBoot, GridFS

## Overview
This Spring Boot application provides an API to manage and deliver images stored in MongoDB GridFS, including fetching random images with metadata for seamless frontend integration.

---

# Connect to your MongoDB database
```
use imageDb;  
```

# Create collections for GridFS bucket "images"
```
db.createCollection("images");
db.createCollection("images.files");
db.createCollection("images.chunks");
```
# Optionally, create an index on the "images.files" collection for the "filename" field
```
db["images.files"].createIndex({ "filename": 1 });
```

# Optionally, create an index on the "images.chunks" collection for the "files_id" field (to improve performance for queries on the chunks)
```
db["images.chunks"].createIndex({ "files_id": 1 });
```

# Create the user with readWrite access to the database. incase this is not working, use the atlas UI to create user and roles
```
db.createUser({
  user: "imageDbRW",       
  pwd: "imageUserRW",      
  roles: [
    {
      role: "readWrite",   
      db: "imageDb"        
    }
  ]
});
```

# Optionally, create a custom role for access to specific collections
```
db.createRole({
  role: "imageAccess",
  privileges: [
    {
      resource: { db: "your_database_name", collection: "images.files" },
      actions: ["find", "insert", "update", "remove"]
    },
    {
      resource: { db: "your_database_name", collection: "images.chunks" },
      actions: ["find", "insert", "update", "remove"]
    }
  ],
  roles: []
});
```

# Create a user with the custom role
```
db.createUser({
  user: "image_user",
  pwd: "image_user_password",
  roles: [
    { role: "imageAccess", db: "your_database_name" }
  ]
});
```

---

## How to Use

1. **Clone the repository**

```
git clone https://github.com/sriganth3/infinite-scroll-backend.git
cd infinite-scroll-backend
```

2. **Build the application using Maven**

```
mvn clean install
```

3. **Run the application**

```
mvn spring-boot:run
```

4. **Access the Swagger documentation**
After the application is up and running, open your browser and navigate to:

```
http://localhost:8080/swagger-ui.html
```

This will display the Swagger UI where you can view and interact with the REST APIs.

---