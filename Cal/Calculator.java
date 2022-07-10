package Cal;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class Calculator implements ActionListener{

    JFrame jf;
    JPanel jp;                                              // panel to attach buttons
    JButton[] buttons;                                      // buttons
    String[] btnTag = {"AC", "C", "()", "/",                // buttons string  
                         "7", "8", "9", "*",
                         "4", "5", "6", "-",
                         "1", "2", "3", "+",
                         ".", "0", "="};
    
    JTextField resultField;                                 // result TextField
    String num;                                             // 숫자 일시저장
    Stack<String> stack = new Stack<String> ();             // 후위표기 변환 시에 사용할 스택  
    Stack<Double> numStack = new Stack<Double>();           // 후위표기 계산 시에 사용할 스택
    ArrayList<String> matrix = new ArrayList<String>();     // 버튼 클릭 시 해당 문자 저장할 배열 리스트
    ArrayList<String> postfix = new ArrayList<String>();        // 후위표기 식을 저장할 배열 리스트
    
    int ParenCnt = 1;           //괄호 버튼 클릭 플래그 : 1 -> "(", 0 -> ")" 
    
    GridBagConstraints constraints = new GridBagConstraints();      // layout


    /*생성자*/
    public Calculator(String msg){
        // create frame 
        jf = new JFrame(msg);
        jf.setLayout(new BorderLayout(2, 2));
        jf.setBackground(new Color(233, 233, 233));
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // create result textfield
        resultField = new JTextField(60);
        resultField.setFont(new Font("고딕", Font.BOLD, 50));
        jf.add(resultField, BorderLayout.NORTH);

        // create panel to attach buttons
        jp = new JPanel(new GridBagLayout());
        jp.setSize(300, 400);

        constraints.fill = GridBagConstraints.BOTH;                                      // 여백 모두 채움
        constraints.insets = new Insets(3, 2, 3, 2);             // 외부 패딩 지정
        constraints.weightx = 1.0;                                                       // 창 크기 변할 때 컴포넌트 크기 변화
        constraints.weighty = 1.0;
        
        // var to position btns
        int x = 0;
        int y = 0;

        // create buttons and attach
        buttons = new JButton[btnTag.length];

        for(int i = 0; i < btnTag.length; i++){                
            buttons[i] = new JButton(btnTag[i]);
            buttons[i].setFont(new Font("Serif", Font.BOLD, 30));
            buttons[i].addActionListener(this);

            x = i % 4; // col == 4 -> to position the buttons

            // Conditional statement for adding color to a button
            if (i == 0 || i == 1 || i ==2) {
                buttons[i].setBackground(new Color(205, 204, 203));
            } else if (i == 3 || i == 7 || i == 11 || i == 15 || i == 18) {
                buttons[i].setBackground(new Color(255, 158, 11));
            } else {
                buttons[i].setBackground(new Color(115, 115, 115));
            }

            // At the end = conditional statement to create a button
            if (i == 18) {
                attachBtn(buttons[i], x, y, 2, 1);
                break;
            }

            // Code to create a button
            attachBtn(buttons[i], x, y, 1, 1);

            // Conditional statement for adjusting the coordinates of the button
            if (x == 3) {
                y++;
            }
        }

        jf.add(jp, BorderLayout.CENTER);
        jf.setSize(350, 500);
        jf.setVisible(true);

    }

    
    /*Event Handling*/
    @Override
    public void actionPerformed(ActionEvent e){        
    
        String tmp = e.getActionCommand();   
        int lastIndex = matrix.size()-1;

        if(tmp != "="){
         
            if(tmp == "AC"){                                  // AC: 모두 clear
                matrix.clear();
                stack.clear();
                numStack.clear();
                postfix.clear();

            }
            
            else if(tmp == "C"){                              // C: 마지막 문자 하나만 제거
                
                if(matrix.get(lastIndex) == "("){       // 만약 제거할 문자가 ()이면 플래그 수정해줘야함. 1 -> "(", 0 -> ")" 
                    ParenCnt = 1;                             // "("를 지웠으므로 다시 1로 바꿔줌
                }
                else if(matrix.get(lastIndex) == ")"){
                    ParenCnt = 0;                             // ")"을 지웠으므로 다시 0으로 바꿔줌
                }
                                                              // 나머지 경우는 그냥 지워주면 됨
                matrix.remove(lastIndex); 
                              
            }

            else if(tmp == "()"){                             // (): 괄호
                if(ParenCnt == 1){                            // 여는 괄호, 플래그 수정
                    matrix.add("(");                                
                    ParenCnt = 0;   
                }
                else{
                    matrix.add(")");                        // 닫는 괄호, 플래그 수정
                    ParenCnt = 1;
                }
            }

            else if(tmp == "."){
                if(matrix.isEmpty()){
                    matrix.add("0.");
                }
                else{
                    String lastString = matrix.get(lastIndex);

                    if(isNumeric(lastString)){     //matrix의 맨 마지막이 숫자이면 거기에 넣어줌
                        lastString += tmp;
                        matrix.remove(lastIndex);
                        matrix.add(lastString);
                    }
                }
            }

            else if(tmp == "+" || tmp == "-" || tmp == "*" || tmp == "/"){      //연산자일 경우
                matrix.add(tmp);                  //matrix에 넣어줌
            }

            else{                   //숫자일 경우엔
                if(!matrix.isEmpty()){
                    String lastString = matrix.get(lastIndex);

                    if(isNumeric(lastString) || lastString.endsWith(".")){     //matrix의 맨 마지막이 숫자, 혹은 소수점이면 거기에 넣어줌
                        lastString += tmp;
                        matrix.remove(lastIndex);
                        matrix.add(lastString);
                    }
                    else{
                        matrix.add(tmp);
                    }
                }
                else{
                    matrix.add(tmp);
                }
            }

            // matrix를 결과창에 출력
            String total = ""; 
            for(int i=0; i<matrix.size(); i++){
                total += matrix.get(i);
            }

            resultField.setText(total);
        }

        if(tmp == "="){
            // = 을 누른 경우
            // 후위표기로 변환
            Infix_to_Postfix(matrix, postfix);
    
            // 후위표기 계산
            Double result = evalPostfix(postfix);
            String res = String.valueOf(result);
    
            // 결과 출력
            resultField.setText(res);

            //다음 연산을 위해 배열 모두 클리어
            matrix.clear();
            matrix.add(res);        //다음 연산을 이어가기 위해 결과를 남겨줌
            stack.clear();
            numStack.clear();
            postfix.clear();

        }
        
    }
   
    /*버튼 붙이는 함수*/
    public void attachBtn(Component c, int x, int y, int w, int h) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = w;
        constraints.gridheight = h;
    
        jp.add(c, constraints);
    }
    
    /*후위 표기식 변환 함수*/
    public void Infix_to_Postfix(ArrayList<String> infixStr, ArrayList<String> postfixStr){
        String tmp;

        //전달받은 배열을 왼쪽부터 하나씩 검사
        for(int i=0; i<infixStr.size(); i++){
            String ch = infixStr.get(i);

            switch(ch){                 
                case "(":                                               //괄호 : 스택에 삽입
                    stack.push(ch);
                    break;
                
                case "*": case "/":
                    if(stack.isEmpty()){
                        stack.push(ch);
                        break;
                    }                                                        //곱셈, 나눗셈 
                    else if(stack.peek() == "*" || stack.peek() == "/"){     //스택에 탑이 곱하기 나누기면 스택을 pop하고 현재 연산자를 push
                        tmp = stack.pop();
                        postfixStr.add(tmp);
                        stack.push(ch);
                        break;
                    }
                    else{
                        stack.push(ch);                                     //아니면 모두 push
                        break;
                    }

                case "+": case "-":                                     //덧셈, 뺄셈 
                    if(stack.isEmpty()){                                //스택 비어있으면 그냥 push
                        stack.push(ch);
                        break;
                    }
                    else if(stack.peek() == "("){                       //마지막이 ( 이면 그냥 푸쉬
                        stack.push(ch);                 
                        break;
                    }
                    else{                                               //나머지 경우는 스택의 탑을 pop, 현 연산자 push
                        tmp = stack.pop();
                        postfixStr.add(tmp);
                        stack.push(ch);
                        break;
                    }

                case ")":                                   //닫는 괄호 : (를 만날때까지 스택을 pop, (도 지워준다
                    while(stack.peek() != "("){
                        tmp = stack.pop();
                        postfixStr.add(tmp);
                    }
                    stack.pop();
                    break;

                default:                                    //숫자는 후위표기 배열에 바로 삽입
                    postfixStr.add(ch);
                    break;
            }
        }

        while(!stack.isEmpty()){              //스택이 빌때 까지 pop
            postfixStr.add(stack.pop());
        }
    }

    /*문자인지 숫자인지 판별해주는 함수 */
    public static boolean isNumeric(String str) { 
        try {  
          Double.parseDouble(str);  
          return true;
        } catch(NumberFormatException e){  
          return false;  
        }  
      }

    
    /*후위 표기식 계산 함수 */
    public double evalPostfix(ArrayList<String> postfixStr){

        for(int i=0; i<postfixStr.size(); i++){            //숫자이면 스택에 push, 연산자면 두개를 pop하여 계산한 결과를 다시 push
            
            String ch = postfixStr.get(i);
            
            if(isNumeric(ch)){
                numStack.push(Double.valueOf(ch));
            }
            else{
                Double a = numStack.pop();
                Double b = numStack.pop();
                String op = ch;

                switch(op){
                    case "+":
                        numStack.push(b + a); break;
                    case "-":
                        numStack.push(b - a); break;
                    case "*":
                        numStack.push(b * a); break;
                    case "/":
                        numStack.push(b / a); break;
                }
            }
        }

        Double res = numStack.pop();                //계산 결과 리턴

        return res;
    }
 }

