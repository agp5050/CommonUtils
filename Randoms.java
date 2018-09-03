public class Randoms {
    public static List<Integer> choice(int start,int end,boolean replaceable,int num) throws Exception {
        List<Integer> list;
        int volume=end-start+1;
        if (volume<1|| num<1){
            throw new Exception(String.format("%d is smaller than %d ! Or %d is smaller than 1",end,start,num));
        }
        if (replaceable){
            list=new ArrayList<>();
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
            list=new ArrayList<>();
            list.addAll(set);
            return list;
        }
    }

    public static void main(String[] args) throws Exception {
        List<Integer> list=Randoms.choice(1,1,false,1);
        System.out.println(list);
    }
}
