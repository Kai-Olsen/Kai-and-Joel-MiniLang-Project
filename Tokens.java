// Creating a Tokens class in order to each token type with its value.
    public class Tokens {
        public String type;
        public String value;

        // Constructor for Tokens 
        public Tokens(String t, String v){
            type = t;
            value = v;            
        }

        @Override //overrides the default toString for our trace mode
        public String toString(){
            return "[TOKEN] \t" + type + "[" + value + "]";
        }
    }