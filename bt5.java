/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package oopjva;
import java.util.Scanner;
/**
 *
 * @author daoho
 */
public class bt5 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        long N= 56789;
        int chusoDau=0;
        while (N>0){
            chusoDau=(int)(N%10);
            N/=10;
        }
        System.out.println("the first number of N is: "+chusoDau);
    }


public class Main {
    public static void main(String[] args) {
      // TODO code application logic here
        long N= 56789;
      int  sum= 0;
        for (int i=0;i<N%10; i++)
        {
        sum += N%10;
           }
        

System.out.println("Tổng các chữ số của N là: " + sum);
 
    
                                    }
                    }

public class Main {
     public static void main(String[] args) {

long N = 12343765;
int maxDigit = 0;
    while( N>0)  {
    int digit = (int) (N % 10);
    if (digit > maxDigit) {
        maxDigit = digit;
    }
    N /= 10;
            }
    System.out.println("Chữ số lớn nhất trong k chữ số của N là: " + maxDigit);

}
     public class Main {
     public static void main(String[] args) {
long N = 123456789;
int count = 0;
while (N > 0) {
    count++;
    N /= 10;
}
System.out.println("Số chữ số của N là: " + count);

}
     }

