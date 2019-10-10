

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

import java.util.List;

public class PageUtils {
    public static <T> Page splitList(List<T> content, Pageable pageRequest) {
        Assert.notNull(content, "Content must not be null!");
        Assert.notNull(pageRequest, "pageRequest must not be null!");
        long offset = pageRequest.getOffset();
        int pageSize = pageRequest.getPageSize();
        int size = content.size();
        int max_complete_page= size/pageSize;
        //page num 从0 开始最大不能大于 total/size.
        int plus= size%pageSize==0 ? -1 : 0;
        int max_page_num=max_complete_page+plus;
        if (pageRequest.getPageNumber()> max_page_num)
            pageRequest= PageRequest.of(max_page_num,pageSize);
        if (size<=pageSize)
            return new PageImpl(content,pageRequest,size);
        if (offset<size && (offset+pageSize>size))
            return new PageImpl(content.subList(new Long(offset).intValue(),size),pageRequest,size);
        return new PageImpl(content.subList(new Long(offset).intValue(),new Long(offset+pageSize).intValue()),pageRequest,size);

    }
}
