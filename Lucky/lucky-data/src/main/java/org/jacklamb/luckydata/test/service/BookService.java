package org.jacklamb.luckydata.test.service;

import org.jacklamb.luckydata.test.mapper.BookMapper;
import org.luckyframework.context.annotation.Service;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/2 0002 13:14
 */
@Service
public class BookService {

    private final BookMapper bookMapper;

    public BookService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public void printAllBook(){
        System.out.println(bookMapper.selectList());
    }
}
