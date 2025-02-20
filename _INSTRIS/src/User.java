class UserResponse {
    private Data data;
    public Data getData() {
        return data;
    }

    static class Data {
        private User user;

        public User getUser() {
            return user;
        }
    }

    static class User {
        private String biography;
        private String username;
        private String full_name;
        private String id;

        public String getBiography() {
            return biography;
        }

        public String getId() {
            return id;
        }

        public String getFullName() {
            return full_name;
        }

        public String getUserName() {
            return username;
        }
    }
}