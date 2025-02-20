class MediaResponse {
    private Item[] items;
    public Item[] getItem(){
        return items;
    }

    static class Item{
        private String pk;
        private int like_count;
        private Owner owner;

        public String getMediaId(){
            return pk;
        }

        public int getLikeCount() {
            return like_count;
        }

        public Owner getOwner(){
            return owner;
        }
        static class Owner{
            private String username, full_name, id;
            public String getUserName(){
                return username;
            }

            public String getFullName(){
                return full_name;
            }

            public String getId() {
                return id;
            }
        }
    }
}
