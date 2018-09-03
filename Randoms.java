import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class Randoms {
    /**
     * 翻译python numpy.random.choice(start,end,num,replace=False)
     */
    public static List<Integer> choice(int start,int end,boolean replaceable,int num,List<Double> weights) throws Exception {
        List<Integer> list=new ArrayList<>();
        int volume=end-start+1;
        if (volume<1|| num<1){
            throw new Exception(String.format("%d is smaller than %d ! Or %d is smaller than 1",end,start,num));
        }

        if (weights==null){
            if (replaceable){
                for (int i=0;i<num;i++){
                    int rst=(int)(Math.random()*volume)+start;
                    if (rst==end && rst!=start){
                        rst=rst-1;//左闭右开
                    }
                    list.add(rst);
                }
                return list;
            }else {
                if (volume<num){
                    throw new Exception(String.format("numbers in range(%d,%d) is less than num:%d ! ",start,end,num));
                }
                Set<Integer> set=new HashSet<>();
                while (set.size()<num){
                    int rst=(int)(Math.random()*volume)+start;
                    if (rst==end && rst!=start){
                        rst=rst-1;//左闭右开
                    }
                    set.add(rst);
                }
                list.addAll(set);
                return list;
            }
        }
        List<Integer> listTmp=new ArrayList<>();
        for (int i=start;i<end;i++){
            listTmp.add(i);
        }

        list.addAll(choice(listTmp,num,replaceable,weights));

        return list;


    }


    /**
     * @param collection
     * @param num
     * @param replaceable
     * @param weights
     * @param <T>
     * @return
     *
     *      * 翻译python numpy.random.choice(a,num,replace=False,p=weights)
     *      * a : 1-D array-like or int
     *      * 从a集合中抽取mum个对象，抽取的概率应该是p集合概率中的对应位置的概率
     */
    public static <T> Collection<T> choice(Collection<T> collection,int num,boolean replaceable,List<Double> weights) throws Exception {
        Collection<T> collectionRst;
        if(!replaceable && collection.size()<num){
            throw new Exception(String.format("collection size %d is smaller than selected number:num %d on no replaceable condition! ",collection.size(),num));
        }
        if (weights.size()!= collection.size()){
            throw new Exception(String.format("num:%d must be equal to collection size:%d ",weights.size(),collection.size()));
        }
        double rate=0;
        for (double weight:weights){
            if (weight<0){
                throw new Exception("weights  items must all be non-negative");
            }
            rate+=weight;
        }
        if (rate!=1){
            throw new Exception("sum of weights  must be equal to 1.0 ");
        }

        validNumInCollection(collection,num);


        //remove weight=0 from weights list; and same time remove the  weight relative value on collection position.
        List<T> list=new ArrayList<>();
        list.addAll(collection);

        int i=0;
        double sum=0;
        List<Double> threshold=new ArrayList<>();
        weights=new CopyOnWriteArrayList<>(weights);
        for (double weight:weights){
            if (weight==0d){
                list.remove(i);//将色子列表中对应权重为0的移除
                weights.remove(i);//权重列表中也对应删除为0的值
            }else { //权重不为0则加入到sum，将这个sum作为阈值表的阈值。
                sum+=weight;
                threshold.add(sum);
            }
            i++;
        }
        if (replaceable){
            collectionRst=new ArrayList<>();
            while (collectionRst.size()<num){
               double randomV= Math.random();
                int c=0;
               for (double weight:threshold){
                   if (randomV<=weight){
                       collectionRst.add(list.get(c));
                       break;
                   }
                   c++;
               }
            }
        }else{
            collectionRst=new HashSet<>();//不可重复选项时，高概率的出现次数被拉低了，低概率的被拉高
            validNumInCollection(list,num);
            while (collectionRst.size()<num){
                double randomV= Math.random();
                int index=0;
                for (double v:threshold){ //只需要在阈值列表中比对即可
                    if (randomV<=v){
                        collectionRst.add(list.get(index));
                        break;
                    }
                    index++;
                }
            }
        }

        return collectionRst;
    }

    private static <T> void validNumInCollection(Collection<T> collection, int num) throws Exception {
        Set<T> set=new HashSet<>(collection);
        if (set.size()<num){
            throw new Exception(String.format("collection has not enough distinct elements than num:%d ",num));
        }
    }

    public static void main(String[] args) throws Exception {
        Double[] doubles=new Double[]{0.1,0.1,0.4,0.0,0.4};
        List<Integer> list=Randoms.choice(1,6,false,2,Arrays.asList(doubles));
        System.out.println(list);
       /* Double[] doubles=new Double[]{0.1,0.1,0.4,0.0,0.4};
        Arrays.asList(doubles);

        for (int count=0;count<20;count++){
            int a=0;
            int b=0;
            int c=0;
            int d=0;
            int e=0;
            for (int i2=0;i2<20000;i2++){
                Collection<Character> collection=Randoms.choice(Chars.asList("ABCDE".toCharArray()),3,false,Arrays.asList(doubles));
                for (char item:collection){
                    if (item=='A')
                        a+=1;
                    if (item=='B')
                        b+=1;
                    if (item=='C')
                        c+=1;
                    if (item=='D')
                        d+=1;
                    if (item=='E')
                        e+=1;
                }

            }
            System.out.println(String.format("A:%d\t B:%d\t C:%d\t D:%d\tE:%d",a,b,c,d,e));
        }*/
    }

}
