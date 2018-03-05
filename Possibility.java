

//should be noted that the right and lower border are non inclusive
public class Possibility {
    private int left, right, up, down;
    private int size;
    private int weight;
    private boolean corner;
    private int limit;

    public Possibility(int left, int right, int up, int down,char pizza[][], char smaller, int limit){
        this.left = left;
        this.right = right;
        this.up = up;
        this.down = down;
        size = calcSize();
        weight = calcWeight(pizza,smaller);
        corner = (left == 0) || (right == pizza[0].length) || (up == 0) || (down == pizza.length);
        this.limit = limit;
    }

    //in epanding the slice if it is down and if it is right it should still be included
    //since by the char parameter we can now the direction of the expanding process
    //we can expand to only one direction, i.e. it is the left neighbour then that will
    //expan only to the right and nowhere else
    //this funciton can be generalized to not use the char and be less error prone,
    //however this is just for the beginning
    public void expand(int x, char d){
        if (d == 'L') {
            right += x;
            size = calcSize();
            //we don't care about the weight anymore as the ones left should be taken anyway
        }
        else if(d == 'R'){
            left -= x;
            size = calcSize();
        }
        else if(d == 'D'){
            up -= x;
            size = calcSize();
        }
        else if(d == 'U'){ //there is the possibility that there is no solution for that point at the moment
            down += x;
            size = calcSize();
        }
    }

    //this function return a 2d array with two values indicating on and if the expand of this possibility is possible
    //output [ x L inc, X R inc, Y U inc, Y D inc, size extra increase]
    public int[] expandingCapabilities(int x, int y){
        int[] expandingAmount = {0,0,0,0,0};
        if(x >= right ){
            expandingAmount[1] = x - right + 1;//we need the +1 because right is non-inclusive
            expandingAmount[0] = 0;
        }
        else if(x < left){
            expandingAmount[0] = left - x;
            expandingAmount[1] = 0;
        }
        if(y >= down){
            expandingAmount[3] = y - down + 1;
            expandingAmount[2] = 0;
        }
        else if(y < up) {
            expandingAmount[2] = up - y;
            expandingAmount[3] = 0;
        }
        //if with this extension goes over the limit (increses size too much) then we do not move forward
        int newPossibleSize = (right - left) * (expandingAmount[2] + expandingAmount[3]) + (down - up) * (expandingAmount[1] + expandingAmount [0]);//(expandingAmount[1] - expandingAmount[0]) * (expandingAmount[2] - expandingAmount[3]);
        expandingAmount[4] = newPossibleSize;
        newPossibleSize += size;
        if(newPossibleSize > limit){

            expandingAmount[4] = -1;
        }
        return expandingAmount;
    }

    public int growingPossibility(){
        return limit - size;
    }
    public boolean canExpand(){
        return limit != size;
    }
    private int calcSize(){
        return (right - left) * (down - up);
    }

    private int calcWeight(char pizza[][],char smaller){
        int weight = 0;
        for(int i = left; i < right; i++){
            for(int j = up; j < down; j++){
                if(pizza[j][i] == smaller){
                    weight++;
                }
            }
        }
        return weight;
    }

    public boolean isCorner(){
        return corner;
    }

    public int getSize(){
        return size;
    }

    public int getWeight(){
        return weight;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getUp() {
        return up;
    }

    public int getDown() {
        return down;
    }

    @Override
    public String toString() {
        return up + " " + left + " " + (down -1) + " " + (right - 1) + "\n";
    }
}
