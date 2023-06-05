import java.util.Objects;

public class Pair<T, U> {
	public T first;
	public U second;
	public Pair(T f, U s){
		this.first = f;
		this.second = s;
	}
	public boolean equals(Pair<T, U> other){
       return this.first.equals(other.first) && this.second.equals(other.second);
    }

    public T getKey() {
        return this.first;
    }

    public U getValue() {
        return this.second;
    }

    public int hashCode(){
      return Objects.hash(first, second);
    }
    
    public String toString(){
    	return "<" + first.toString() + ", " + second.toString() + ">"; 
    }
}