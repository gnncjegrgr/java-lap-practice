import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class Chess extends Frame {
    private Timer timer = null; //타이머

    private final int sql=80; //체스 정사각형 한칸의 길이
    private final int w=1230, h=900; //가로길이 및 세로 길이
    public piece board[][]= new piece [8][8]; //말을 올려 놓을 보드, piece64개를 나타내고 말이 위에 없다면 null값을 가짐

    private boolean[][] onClick = new boolean [8][8]; //클릭되었는지 확인
    private boolean firstClick = true; //첫 번째 클릭인지 확인
    private boolean Moveable[][] = new boolean[8][8]; //해당 말이 움직일 수 있는 공간인지 확인

    private boolean bqc, bkc, wqc, wkc; //캐슬링 가능 여부, black queen castling

    private String turn = "white"; //누구의 순서인지 나타내는 변수
    
    private int ci, cj;
    private int startTime=300; //처음에 주어지는 시간
    private int inc=20; //매 수마다 추가되는 시간
    private int wtime, btime; //백과 흑이 각각 남은 시간
    private int nmoves; //총 움직임 횟수
    
    private Label lWhiteTimer = null; //백의 남은 시간을 나타내는 라벨
    private Label lBlackTimer = null; //흑의 남은 시간을 나타내는 라벨

    private JButton bForfeit = null; //항복
    private JButton bDraw = null; //무승부 하자는 신호 
    private JButton bStart = null; //재시작
    private JButton bSettime = null; //시간 설정 

    private JButton bBoard[][] = new JButton[8][8]; //전체 보드

    private ImageIcon icon_light = new ImageIcon("pic/light.png"); //밝은 칸 icon
    private ImageIcon icon_dark = new ImageIcon("pic/dark.png"); //어두운 칸을 icon

    class BoardState{ //체스판의 상태를 저장
        int board[][] = new int[8][8];
        String turn;
        boolean bqc, bkc, wqc, wkc;
        
    }

    BoardState[] bstate = new BoardState[600];

    abstract class piece{ //말 클래스
        int i, j; //말의 위치 
        String color, boardcolor, name; //말의 색, 말이 위치한 보드 칸의 색, 말의 종류
        int ind; //말의 종류를 나타내는 정수 => BoardState클래스에는 board[][]를 int로 설정했기 때문에 int 타입의 ind로 표시
        ImageIcon Icon, clickIcon; //그냥 있을 때 아이콘, 클릭되었을 때 아이콘 

        void move(int a, int b){ //말을 찍은 위치로 이동
        	
            this.i=a;
            this.j=b;
            if ((this.i+this.j)%2==0) this.boardcolor = "light";
            else this.boardcolor = "dark";

            Icon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+".png");
            clickIcon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+"_2.png");
        }
        
        
        abstract void setMoveable(); //움직일 수 있는 공간 결정
    }

    class Pawn extends piece{ //폰 클래스 구현
        Pawn(int a, int b, String c){ //생성자로 초기값 결정
            this.i=a;
            this.j=b;
            this.color=c;
            this.name="pawn";

            if ((this.i+this.j)%2==0) this.boardcolor = "light";
            else this.boardcolor = "dark";
            Icon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+".png");
           
            clickIcon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+"_2.png");
            this.ind=1;
        }

        @Override
        void move(int a, int b){ 
            this.i=a;
            this.j=b;
            if ((this.i+this.j)%2==0) this.boardcolor = "light";
            else this.boardcolor = "dark";

            if(this.i==0 || this.i==7){ //폰이 끝에 도달했을 때
                Object[] promotion = {"Knight", "Bishop", "Rook", "Queen"};
                Label label = new Label("Promote to another piece: ");
                label.setFont(new Font("Arial", Font.PLAIN, 20));
                String s = (String) JOptionPane.showInputDialog(null, label, "Promotion", JOptionPane.PLAIN_MESSAGE, null, promotion, "Queen");
                promote(this.i, this.j, s, this.color);
            }
            Icon = new ImageIcon("pic/" + this.color + "_" + this.name + "_" + this.boardcolor + ".png");
            clickIcon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+"_2.png");
        }

        void setMoveable(){
            if(this.color=="black"){
                for(int i=0; i<8; i++){
                    for(int j=0; j<8; j++){
                        if(this.i+1 == i){
                            if(board[i][j]==null){
                                if(j==this.j) {
                                	Moveable[i][j]=true; //폰은 앞으로 한 칸 전진할 수 있다
                                	continue;
                                }
                                else{
                                	Moveable[i][j]=false;
                                	continue;
                                }
                            }
                            else if(board[i][j].color=="white" && (Math.abs(this.j-j)==1)) {
                            	Moveable[i][j]=true; //폰은 상대 말을 잡을 때에는 대각선으로 한 칸 이동한다
                            	continue;
                            }
                            else {
                            	Moveable[i][j]=false;
                            	continue;
                            }
                        }
                        else {
                        	Moveable[i][j]=false;
                        	continue;
                        }
                    }
                }
                if(this.i==1 && board[3][j]==null) Moveable[3][j]=true; //처음 움직이는 폰은 두 칸 움직일 수 있다
            }
            else{
                for(int i=0; i<8; i++){
                    for(int j=0; j<8; j++){
                        if(this.i-1 == i){
                            if(board[i][j]==null){
                                if(j==this.j) {
                                	Moveable[i][j]=true;
                                	continue;
                                }
                                else {
                                	Moveable[i][j]=false;
                                	continue;
                                }
                            }
                            else if(board[i][j].color=="black" && (Math.abs(this.j-j)==1)) {
                            	Moveable[i][j]=true;
                            	continue;
                            }
                            else {
                            	Moveable[i][j]=false;
                            	continue;
                            }
                        }
                        else Moveable[i][j]=false;
                    }
                    if(this.i==6 && board[4][j]==null) {
                    	Moveable[4][j]=true;
                    	continue;
                    }
                }
            }
        }
    }

    class Knight extends piece{ //나이트
        Knight(int a, int b, String c){ //생성자로 초기값 설정
            this.i=a;
            this.j=b;
            this.color=c;
            this.name="knight";

            if ((this.i+this.j)%2==0) this.boardcolor = "light";
            else this.boardcolor = "dark";
            Icon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+".png");
            clickIcon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+"_2.png");
           
            this.ind=2;
        }

        void setMoveable(){
            if(this.color=="black"){
                for(int i=0; i<8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if ((Math.abs(this.i-i) == 2 && Math.abs(this.j-j) == 1) || (Math.abs(this.i-i) == 1 && Math.abs(this.j-j) == 2)) { //나이트는 한 방향으로 두 칸 이동하고 수직한 방향으로 한 칸 이동한다
                            if (board[i][j] == null) Moveable[i][j] = true;
                            else if (board[i][j].color == "white") Moveable[i][j] = true; //이동한 최종 위치에 상대 기물이 있으면 잡을 수 있다
                            else Moveable[i][j] = false;
                        }
                        else Moveable[i][j] = false;
                    }
                }
            }
            if(this.color=="white"){
                for(int i=0; i<8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if ((Math.abs(this.i-i) == 2 && Math.abs(this.j-j) == 1) || (Math.abs(this.i-i) == 1 && Math.abs(this.j-j) == 2)) {
                            if (board[i][j] == null) Moveable[i][j] = true;
                            else if (board[i][j].color == "black") Moveable[i][j] = true;
                            else Moveable[i][j] = false;
                        }
                        else Moveable[i][j] = false;
                    }
                }
            }
        }
    }

    class Bishop extends piece{ //비숍 클래스
        Bishop(int a, int b, String c){ 
            this.i=a;
            this.j=b;
            this.color=c;
            this.name="bishop";

            if ((this.i+this.j)%2==0) this.boardcolor = "light";
            else this.boardcolor = "dark";
            Icon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+".png");
            clickIcon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+"_2.png");
            
            this.ind=3;
        }

        void setMoveable(){
            for(int i=0; i<8; i++) for(int j=0; j<8; j++) Moveable[i][j]=false;

            if(this.color=="black"){ 
                int i=this.i+1;
                int j=this.j+1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="black") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i++; j++; //우측 하단 방향
                }

                i=this.i+1; j=this.j-1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="black") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i++; j--; //좌측 하단 방향
                }

                i=this.i-1; j=this.j+1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="black") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i--; j++; //우측 상단 방향
                }

                i=this.i-1; j=this.j-1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="black") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i--; j--; //좌측 상단 방향
                }
            }

            else{
                int i=this.i+1, j=this.j+1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="white") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i++; j++;
                }

                i=this.i+1; j=this.j-1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="white") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i++; j--;
                }

                i=this.i-1; j=this.j+1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="white") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i--; j++;
                }

                i=this.i-1; j=this.j-1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="white") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i--; j--;
                }
            }
        }
    }

    class Rook extends piece{ //룩 클래스 구현
        Rook(int a, int b, String c){ //생성자를 통한 초기값 설정
            this.i=a;
            this.j=b;
            this.color=c;
            this.name="rook";

            if ((this.i+this.j)%2==0) this.boardcolor = "light";
            else this.boardcolor = "dark";
            Icon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+".png");
            clickIcon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+"_2.png");
            
            this.ind=4;
        }

        @Override
        void move(int a, int b){ //캐슬링 관련 규칙을 위한 Overriding
            if(this.j==0 && this.color=="black") bqc=false; //룩이 움직이면 그 방향으로는 캐슬링할 수 없다
            if(this.j==7 && this.color=="black") bkc=false;
            if(this.j==0 && this.color=="white") wqc=false;
            if(this.j==0 && this.color=="white") wkc=false;

            this.i=a;
            this.j=b;
            if ((this.i+this.j)%2==0) this.boardcolor = "light";
            else this.boardcolor = "dark";

            Icon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+".png");
        }

        void setMoveable(){ //룩은 평행선으로 원하는 만큼 이동할 수 있다. 단, 앞에 말이 가로막고 있으면 이동할 수 없고 상대 말이면 그 말을 잡을 수 있다
            for(int i=0; i<8; i++) for(int j=0; j<8; j++) Moveable[i][j]=false;
            if(this.color=="black"){
                for(int i=this.i+1; i<8; i++){ //아래쪽 방향
                    if(board[i][this.j]==null) Moveable[i][this.j]=true;
                    else if(board[i][this.j].color=="white"){
                        Moveable[i][this.j]=true;
                        break;
                    }
                    else break;
                }

                for(int i=this.i-1; i>=0; i--){ //위쪽 방향
                    if(board[i][this.j]==null) Moveable[i][this.j]=true;
                    else if(board[i][this.j].color=="white"){
                        Moveable[i][this.j]=true;
                        break;
                    }
                    else break;
                }

                for(int j=this.j+1; j<8; j++){ //오른쪽 방향
                    if(board[this.i][j]==null) Moveable[this.i][j]=true;
                    else if(board[this.i][j].color=="white"){
                        Moveable[this.i][j]=true;
                        break;
                    }
                    else break;
                }

                for(int j=this.j-1; j>=0; j--){ //왼쪽 방향
                    if(board[this.i][j]==null) Moveable[this.i][j]=true;
                    else if(board[this.i][j].color=="white"){
                        Moveable[this.i][j]=true;
                        break;
                    }
                    else break;
                }
            }

            if(this.color=="white"){
                for(int i=this.i+1; i<8; i++){
                    if(board[i][this.j]==null) Moveable[i][this.j]=true;
                    else if(board[i][this.j].color=="black"){
                        Moveable[i][this.j]=true;
                        break;
                    }
                    else break;
                }

                for(int i=this.i-1; i>=0; i--){
                    if(board[i][this.j]==null) Moveable[i][this.j]=true;
                    else if(board[i][this.j].color=="black"){
                        Moveable[i][this.j]=true;
                        break;
                    }
                    else break;
                }

                for(int j=this.j+1; j<8; j++){
                    if(board[this.i][j]==null) Moveable[this.i][j]=true;
                    else if(board[this.i][j].color=="black"){
                        Moveable[this.i][j]=true;
                        break;
                    }
                    else break;
                }

                for(int j=this.j-1; j>=0; j--){
                    if(board[this.i][j]==null) Moveable[this.i][j]=true;
                    else if(board[this.i][j].color=="black"){
                        Moveable[this.i][j]=true;
                        break;
                    }
                    else break;
                }
            }
        }
    }

    class Queen extends piece{ //퀸 클래스 구현
        Queen(int a, int b, String c){ //생성자를 통한 초기값 설정
            this.i=a;
            this.j=b;
            this.color=c;
            this.name="queen";

            if ((this.i+this.j)%2==0) this.boardcolor = "light";
            else this.boardcolor = "dark";
            Icon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+".png");
            clickIcon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+"_2.png");
            
            this.ind=5;
        }

        void setMoveable(){ //퀸은 평행선으로, 대각선으로 원하는 만큼 이동할 수 있다. 단, 앞에 말이 가로막고 있으면 이동할 수 없고 상대 말이면 그 말을 잡을 수 있다
            for(int i=0; i<8; i++) for(int j=0; j<8; j++) Moveable[i][j]=false;

            if(this.color=="black"){
                int i=this.i+1, j=this.j+1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="black") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i++; j++; //우측 하단
                }

                i=this.i+1; j=this.j-1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="black") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i++; j--; //좌측 하단
                }

                i=this.i-1; j=this.j+1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="black") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i--; j++; //우측 상단
                }

                i=this.i-1; j=this.j-1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="black") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i--; j--; //좌측 상단
                }

                for(i=this.i+1; i<8; i++){ //아래쪽
                    if(board[i][this.j]==null) Moveable[i][this.j]=true;
                    else if(board[i][this.j].color=="white"){
                        Moveable[i][this.j]=true;
                        break;
                    }
                    else break;
                }

                for(i=this.i-1; i>=0; i--){ //위쪽
                    if(board[i][this.j]==null) Moveable[i][this.j]=true;
                    else if(board[i][this.j].color=="white"){
                        Moveable[i][this.j]=true;
                        break;
                    }
                    else break;
                }

                for(j=this.j+1; j<8; j++){ //오른쪽
                    if(board[this.i][j]==null) Moveable[this.i][j]=true;
                    else if(board[this.i][j].color=="white"){
                        Moveable[this.i][j]=true;
                        break;
                    }
                    else break;
                }

                for(j=this.j-1; j>=0; j--){ //왼쪽
                    if(board[this.i][j]==null) Moveable[this.i][j]=true;
                    else if(board[this.i][j].color=="white"){
                        Moveable[this.i][j]=true;
                        break;
                    }
                    else break;
                }
            }

            if(this.color=="white"){
                int i=this.i+1, j=this.j+1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="white") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i++; j++;
                }

                i=this.i+1; j=this.j-1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="white") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i++; j--;
                }

                i=this.i-1; j=this.j+1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="white") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i--; j++;
                }

                i=this.i-1; j=this.j-1;
                while(0<=i && i<8 && 0<=j && j<8){
                    if(board[i][j]==null) Moveable[i][j]=true;
                    else if(board[i][j].color=="white") break;
                    else{
                        Moveable[i][j]=true;
                        break;
                    }
                    i--; j--;
                }

                for(i=this.i+1; i<8; i++){
                    if(board[i][this.j]==null) Moveable[i][this.j]=true;
                    else if(board[i][this.j].color=="black"){
                        Moveable[i][this.j]=true;
                        break;
                    }
                    else break;
                }

                for(i=this.i-1; i>=0; i--){
                    if(board[i][this.j]==null) Moveable[i][this.j]=true;
                    else if(board[i][this.j].color=="black"){
                        Moveable[i][this.j]=true;
                        break;
                    }
                    else break;
                }

                for(j=this.j+1; j<8; j++){
                    if(board[this.i][j]==null) Moveable[this.i][j]=true;
                    else if(board[this.i][j].color=="black"){
                        Moveable[this.i][j]=true;
                        break;
                    }
                    else break;
                }

                for(j=this.j-1; j>=0; j--){
                    if(board[this.i][j]==null) Moveable[this.i][j]=true;
                    else if(board[this.i][j].color=="black"){
                        Moveable[this.i][j]=true;
                        break;
                    }
                    else break;
                }
            }
        }
    }

    class King extends piece{ //킹 클래스 구현
        King(int a, int b, String c){ //생성자를 통한 초기값 설정
            this.i=a;
            this.j=b;
            this.color=c;
            this.name="king";

            if ((this.i+this.j)%2==0) this.boardcolor = "light";
            else this.boardcolor = "dark";
            Icon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+".png");
            clickIcon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+"_2.png");
           
            this.ind=6;
        }

        void move(int a, int b){ //캐슬링 규정을 위한 Overriding. 킹이 한 번 움직이면 더 이상 캐슬링 할 수 없다
            if(this.color=="black") {bqc=false; bkc=false;}
            if(this.color=="white") {wqc=false; wkc=false;}

            this.i=a;
            this.j=b;
            if ((this.i+this.j)%2==0) this.boardcolor = "light";
            else this.boardcolor = "dark";

            Icon=new ImageIcon("pic/"+this.color+"_"+this.name+"_"+this.boardcolor+".png");
        }

        void setMoveable(){ //킹은 자신 주변 1칸으로 이동할 수 있다
            for(int i=0; i<8; i++) for(int j=0; j<8; j++) Moveable[i][j]=false;
            if(this.color=="black"){
                for(int i=this.i-1; i<=this.i+1; i++){
                    for(int j=this.j-1; j<=this.j+1; j++){
                        if(0<=i && i<8 && 0<=j && j<8){
                            if(board[i][j]==null) Moveable[i][j]=true;
                            else if(board[i][j].color=="white") Moveable[i][j]=true;
                            else continue;
                        }
                    }
                }
                if(bqc && board[0][1]==null && board[0][2]==null && board[0][3]==null) Moveable[0][2]=true;
                if(bkc && board[0][5]==null && board[0][6]==null) Moveable[0][6]=true;
            }
            if(this.color=="white"){
                for(int i=this.i-1; i<=this.i+1; i++){
                    for(int j=this.j-1; j<=this.j+1; j++){
                        if(0<=i && i<8 && 0<=j && j<8){
                            if(board[i][j]==null) Moveable[i][j]=true;
                            else if(board[i][j].color=="black") Moveable[i][j]=true;
                            else continue;
                        }
                    }
                }
                if(wqc && board[7][2]==null && board[7][3]==null && board[7][1]==null) Moveable[7][2]=true;
                if(wkc && board[7][5]==null && board[7][6]==null) Moveable[7][6]=true;
            }
        }
        
        
    }
    


    public static void main(String[] args){
    	new Chess(); //시작
    
    }

    Chess(){
        makeGUI(); //GUI 만들기
        initGame(); //게임 초기화
    }

    void makeGUI() { //GUI 제작
        setSize(w, h); //Frame 크기 설정
        
        Panel controls = new Panel(); //버튼들이 위치할 panel
        Panel labels = new Panel(); //라벨들이 위치할 panel
        add(controls, BorderLayout.SOUTH); //버튼들은 아래쪽
        add(labels, BorderLayout.NORTH); //라벨들은 위쪽

        Font font = new Font("Arial", Font.PLAIN, 20); //폰트 설정

        lWhiteTimer = new Label("White:                    ");
        lBlackTimer = new Label("Black:                    ");
        lWhiteTimer.setSize(new Dimension(150, 50));
        lBlackTimer.setSize(new Dimension(2150, 50));
        lWhiteTimer.setFont(font);
        lBlackTimer.setFont(font);

        bForfeit = new JButton("Forfeit"); //항복 Button
        bDraw = new JButton("Draw"); //무승부 Button
        bStart = new JButton("Start New Game"); //게임 시작 버튼
        bSettime = new JButton("Time Settings"); //시간 제한 설정 버튼
        bForfeit.setPreferredSize(new Dimension(150, 50));
        bForfeit.setFont(font);
        bDraw.setPreferredSize(new Dimension(150, 50));
        bDraw.setFont(font);
        bStart.setPreferredSize(new Dimension(200, 50));
        bStart.setFont(font);
        bSettime.setPreferredSize(new Dimension(200, 50));
        bSettime.setFont(font);

        labels.add(lWhiteTimer);
        labels.add(lBlackTimer);
        controls.add(bForfeit);
        controls.add(bDraw);
        controls.add(bStart);
        controls.add(bSettime);

        Panel board = new Panel(); //체스판을 나타낼 panel
        add(board); //Frame에 추가
        board.setLayout(null);


        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                bBoard[i][j] = new JButton(); //체스판 한 칸
                bBoard[i][j].setSize(sql, sql); //한 칸의 크기 설정
                bBoard[i][j].setLocation(j * sql,i* sql+40); //한 칸의 위치 설정
                board.add(bBoard[i][j]); 

                bBoard[i][j].addActionListener(new ClickListener(i, j));
            }
        }

        setVisible(true); 

        addWindowListener(new MyWindowAdapter()); 

        timer = new Timer(1000, new ActionListener() {  //타이머 설정. 1초마다 시간 1씩 감소
            @Override
            public void actionPerformed(ActionEvent e) {
                if(turn=="white") wtime--;
                else btime--;
                updateTime();
            }
        });

        bStart.addActionListener(new ActionListener() { //다시하기 버튼 설정
            @Override
            public void actionPerformed(ActionEvent e) {
                Label label = new Label("Restart?");
                label.setFont(font);
                int restart = JOptionPane.showConfirmDialog(null, label); //다시 할 지 물어보는 JOptionPane 띄우기
                if(restart==0){
                    initGame(); //재시작
                }
            }
        });

        bForfeit.addActionListener(new ActionListener() { //항복 버튼 설정
            @Override
            public void actionPerformed(ActionEvent e) {
                Label label = new Label("ForFeit?");
                label.setFont(font);
                int forfeit = JOptionPane.showConfirmDialog(null, label); //항복할 지 물어보는 JOptionPane 띄우기
                if(forfeit==0) {
                    Label forfeitLabel = new Label();
                    if (turn == "white") {
                        forfeitLabel.setText("White Forfeits");
                    } else {
                        forfeitLabel.setText("Black Forfeits");
                    }
                    forfeitLabel.setFont(font);
                    JOptionPane.showMessageDialog(null,forfeitLabel, "Game Over !!", JOptionPane.INFORMATION_MESSAGE); //게임이 끝났다고 알림
                    System.exit(1); 
                }
            }
        });

       
        bDraw.addActionListener(new ActionListener() { //무승부 버튼 설정
            @Override
            public void actionPerformed(ActionEvent e){
                Label label = new Label("Accept Draw?");
                label.setFont(font);
                int draw = JOptionPane.showConfirmDialog(null, label); //무승부를 받아들일 것인지 물어봄
                if(draw==0){
                    label.setText("Draw!!");
                    JOptionPane.showMessageDialog(null, label, "Draw" , JOptionPane.INFORMATION_MESSAGE); //무승부임을 나타냄
                    initGame(); //재시작
                }
                else if(draw==1){
                    label.setText("Draw declined");
                    JOptionPane.showMessageDialog(null, label, "Draw declined" , JOptionPane.INFORMATION_MESSAGE); //무승부가 아님을 나타냄
                }
                else{}
            }
        });

        bSettime.addActionListener(new ActionListener(){ //시간 설정
            @Override
            public void actionPerformed(ActionEvent e){
                Label label = new Label("start time in seconds: "); //최대 시간을 초 단위로 받음
                label.setFont(font);
                String s = (String) JOptionPane.showInputDialog(null, label, "Set time", JOptionPane.PLAIN_MESSAGE, null, null, "");
                try{ //정수가 아닌 값을 입력했을 때를 위해
                    startTime = Integer.parseInt(s);
                }
                catch(Exception e1){ }
                label.setText("Increment in seconds: "); //increment(매 수마다 시간 증가분)을 초 단위로 받음
                s=(String) JOptionPane.showInputDialog(null, label, "Set increment", JOptionPane.PLAIN_MESSAGE, null, null, "");
                try{
                    inc = Integer.parseInt(s);
                }
                catch(Exception e2){ }
            }
        });


    }

    boolean checkCheck(String s){ //특정 색에 대해 체크인지 확인하는 메소드
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j++){
                if(board[i][j]!=null) if(board[i][j].color==s){
                    board[i][j].setMoveable();
                    for(int k=0; k<8; k++){
                        for(int l=0; l<8; l++){
                            if(Moveable[k][l] && board[k][l]!=null) if(board[k][l].name=="king") return true; //움직일 수 있고 그 자리에 킹이 있으면 true 반환
                        }
                    }
                }
            }
        }
        return false; 
    }


    
    void updateTime(){ //시간 업데이트
        lWhiteTimer.setText("White: "+wtime/60+" min "+wtime%60+" sec"); //남은 시간
        lBlackTimer.setText("Black: "+btime/60+" min "+btime%60+" sec");
        if(wtime==0 || btime==0){ //한쪽이 시간을 다 쓸때
            Label label;
            if(wtime==0) label = new Label("White loses on time");
            else label = new Label("Black loses on time");
            label.setFont(new Font("Arial", Font.PLAIN, 20));
            JOptionPane.showMessageDialog(null, label, "Game Over!" , JOptionPane.INFORMATION_MESSAGE); //game over
            System.exit(1);
        }
    }


    void initGame() { //게임 시작 함수
        for(int i=0; i<8; i++) for(int j=0; j<8; j++) board[i][j]=null; //모든 값을 초기화
        for(int i=0; i<8; i++) for(int j=0; j<8; j++) onClick[i][j]=false;
        turn = "white";
        // 캐슬링 true로 전환
        bqc=true;
        bkc=true;
        wqc=true;
        wkc=true;

        wtime=startTime;
        btime=startTime;
        nmoves=0;
        bstate[0]=new BoardState();

        for (int i = 0; i < 8; i++){ //폰 초기 위치 설정
            board[1][i] = new Pawn(1, i, "black");
            board[6][i] = new Pawn(6, i, "white");
        }
        //rook
        board[0][0]= new Rook(0, 0, "black"); 
        board[7][0] = new Rook(7, 0, "white"); 
        board[0][7] = new Rook(0, 7, "black"); 
        board[7][7] = new Rook(7, 7, "white"); 
        //knight
        board[0][1]= new Knight(0, 1, "black");
        board[7][1] = new Knight(7, 1, "white");
        board[0][6] = new Knight(0, 6, "black");
        board[7][6] = new Knight(7, 6, "white");
        //bishop
        board[0][2]= new Bishop(0, 2, "black");
        board[7][2] = new Bishop(7, 2, "white");
        board[0][5] = new Bishop(0, 5, "black"); 
        board[7][5] = new Bishop(7, 5, "white");
        //king and queen
        board[0][3]= new Queen(0, 3, "black");
        board[0][4]= new King(0, 4, "black"); 
        board[7][3]= new Queen(7, 3, "white");
        board[7][4]= new King(7, 4, "white"); 

        
        for(int i=0; i<8; i++){ //좌표별 아이콘 설정
            for(int j=0; j<8; j++){
                if(board[i][j]==null && (i+j)%2 == 0) bBoard[i][j].setIcon(icon_light);
                else if(board[i][j]==null && (i+j)%2 == 1) bBoard[i][j].setIcon(icon_dark);
                else bBoard[i][j].setIcon(board[i][j].Icon);
            }
        }

        timer.start(); 
    }

    void promote(int a, int b, String s, String c){ //프로모션 메소드
        if(s=="Queen") board[a][b]=new Queen(a, b, c);
        if(s=="Rook") board[a][b]=new Rook(a, b, c);
        if(s=="Bishop") board[a][b]=new Bishop(a, b, c);
        if(s=="Knight") board[a][b]=new Knight(a, b, c);

        bBoard[a][b].setIcon(board[a][b].Icon);
    }
    
    void checkKingDie() { 
    	boolean wKing=false;
    	boolean bKing=false;
    	for (int i=0;i<8;i++) {
    		for(int j=0;j<8;j++) {
    			if(board[i][j]==null) {
    				continue;
    			}
    			else if(board[i][j].color=="white"&&board[i][j].name=="king") {
    				wKing=true;
    			}
    			else if(board[i][j].color=="black"&&board[i][j].name=="king") {
    				bKing=true;
    			}
    		}
    	}
    	
    	if(wKing==false) {
    		Label label;
    		label=new Label("White king is dead. White loses");
    		label.setFont(new Font("Arial", Font.PLAIN,20));
        	JOptionPane.showMessageDialog(null, label, "Game Over!", JOptionPane.INFORMATION_MESSAGE);
        	
        	System.exit(1);
    	}
    	else if(bKing==false) {
    		Label label;
    		label = new Label("Black king is dead. Black loses");
    		label.setFont(new Font("Arial", Font.PLAIN,20));
        	JOptionPane.showMessageDialog(null, label, "Game Over!", JOptionPane.INFORMATION_MESSAGE);
        	
        	System.exit(1);
    	}
    	
    	
    }

    class ClickListener implements ActionListener{
        private int i, j;
        ClickListener(int i, int j){ 
            this.i=i;
            this.j=j;
        }

        @Override
        public void actionPerformed(ActionEvent e){
            if(onClick[i][j]) { //같은 곳을 두 번 클릭하는 경우
                onClick[i][j]=false; //클릭하지 않은 상태로 돌아감
                firstClick=true; // firstClick을 다시 true
                bBoard[i][j].setIcon(board[i][j].Icon);
                
            }
            else if(firstClick==true){ //첫 번째 클릭을 하는 경우
                if(board[i][j]!=null) { //해당 좌표 위에 말이 위에 있는 경우에만
                    if(board[i][j].color==turn){ //차례가 맞는 경우에만
                        firstClick = false; //다음 클릭은 두 번째 클릭
                        onClick[i][j] = true; //이미 클릭했다고 표기
                        ci = i; //클릭한 값 저장
                        cj = j;
                        board[i][j].setMoveable(); //해당 말이 움직일 수 있는 좌표 저장
                        bBoard[i][j].setIcon(board[i][j].clickIcon); //클릭했다면 아이콘 변경
                        
                    }
                }
            }
            else if(Moveable[i][j]==true){ //두 번째 클릭에서 움직일 수 있는 공간을 택하면
                Thread thread = new Thread(new Runnable(){
                   
                    public void run(){
                        bstate[nmoves].bqc=bqc; //현재 상태를 bstate 배열에 저장
                        bstate[nmoves].bkc=bkc;
                        bstate[nmoves].wqc=wqc;
                        bstate[nmoves].wkc=wkc;
                        for(int x=0; x<8; x++){
                            for(int y=0; y<8; y++){
                                if(board[x][y]!=null) {
                                    bstate[nmoves].board[x][y] = board[x][y].ind;
                                    if (board[x][y].color == "white") {
                                    	bstate[nmoves].board[x][y] += 10;
                                    }
                                }
                                else {
                                	bstate[nmoves].board[x][y]=0;
                                }
                            }
                        }
                        bstate[nmoves].turn=turn;
                        nmoves++; //한 수 움직임

                        if(board[ci][cj].name=="king"){ //캐슬링 관련 처리(한 번에 두 개의 말을 이동하므로)
                            if(board[ci][cj].color=="black"){
                                if(bqc==true && i==0 && j==2){
                                    board[0][3]=board[0][0];
                                    board[0][0]=null;
                                    board[0][3].move(0, 3);
                                    bBoard[0][3].setIcon(board[0][3].Icon);
                                    bBoard[0][0].setIcon(icon_light);
                                    bqc=false;
                                }
                                if(bkc==true && i==0 && j==6){
                                    board[0][5]=board[0][7];
                                    board[0][7]=null;
                                    board[0][5].move(0, 5);
                                    bBoard[0][5].setIcon(board[0][5].Icon);
                                    bBoard[0][7].setIcon(icon_dark);
                                    bkc=false;
                                }
                            }
                            if(board[ci][cj].color=="white"){
                                if(wqc==true && i==7 && j==2){
                                    board[7][3]=board[7][0];
                                    board[7][0]=null;
                                    board[7][3].move(7, 3);
                                    bBoard[7][3].setIcon(board[7][3].Icon);
                                    bBoard[7][0].setIcon(icon_dark);
                                    wqc=false;
                                }
                                if(wkc==true && i==7 && j==6){
                                    board[7][5]=board[7][7];
                                    board[7][7]=null;
                                    board[7][5].move(7, 5);
                                    bBoard[7][5].setIcon(board[7][5].Icon);
                                    bBoard[7][7].setIcon(icon_light);
                                    wkc=false;
                                }
                            }
                        }
                                                
                        board[i][j]=board[ci][cj]; //말 클래스 이동
                        board[ci][cj] = null; //기존에 있던 자리는 빈 상태로 만들기
                        board[i][j].move(i, j); //클래스 내부 값 변경

                        bBoard[i][j].setIcon(board[i][j].Icon); //아이콘 변경
                        if((ci+cj)%2 == 0 ) bBoard[ci][cj].setIcon(icon_light);
                        else bBoard[ci][cj].setIcon(icon_dark);
                        firstClick=true; //클릭하지 않은 상태로 돌아감
                        onClick[ci][cj]=false;
                        onClick[i][j]=false;

                        if(checkCheck(turn)){ //만약 체크이면
                            Label check = new Label("Check!");
                            check.setFont(new Font("Arial", Font.PLAIN, 20));
                            JOptionPane.showMessageDialog(null, check, "Check!!!" , JOptionPane.INFORMATION_MESSAGE);
                        }
                        if(turn=="white"){
                            wtime+=inc;
                            updateTime();
                            turn="black";
                        }
                        else{
                            btime+=inc;
                            updateTime();
                            turn="white";
                        }
                        
                        
                        checkKingDie();
                        
                        bstate[nmoves]=new BoardState(); //새로운 boardState만들기

                    }
                });
                thread.start();
            }
        }
    }

    class MyWindowAdapter extends WindowAdapter{
        public void windowClosing(WindowEvent e){
            System.exit(0);
        }
        
    }
  
}
    