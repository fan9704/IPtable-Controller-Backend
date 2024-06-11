db.createUser(
    {
      user: "test",
      pwd: "123456",
         roles: [
            {role: 'readWrite', db: 'network', collection: 'network'},
            {role: 'readWrite', db: 'network'},
            {role: 'readWrite', db: 'admin'},
             { role: "userAdminAnyDatabase", db: "admin" }
          ]
    }
);

// Enter in use admin/network database