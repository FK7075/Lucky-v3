package org.jacklamb.luckydata.test.entity;

import com.lucky.jacklamb.annotation.table.Id;
import com.lucky.jacklamb.annotation.table.Table;
import com.lucky.jacklamb.enums.PrimaryType;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/2 0002 11:01
 */
@Table("book")
public class Book {

    @Id(type = PrimaryType.AUTO_INT)
    private Integer id;
    private String name;
    private Double price;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
