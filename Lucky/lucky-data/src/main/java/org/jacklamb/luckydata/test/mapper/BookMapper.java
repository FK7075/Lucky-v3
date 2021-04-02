package org.jacklamb.luckydata.test.mapper;

import com.lucky.jacklamb.mapper.LuckyMapper;
import org.jacklamb.luckydata.annotation.Mapper;
import org.jacklamb.luckydata.test.entity.Book;

import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/2 0002 11:01
 */
@Mapper
public interface BookMapper extends LuckyMapper<Book> {

    List<String> findNameByPriceLessThanEqual(Double maxPrice);
}
