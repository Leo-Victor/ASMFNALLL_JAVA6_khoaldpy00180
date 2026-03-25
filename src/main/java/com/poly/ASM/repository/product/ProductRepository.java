package com.poly.ASM.repository.product;

import com.poly.ASM.entity.product.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @EntityGraph(attributePaths = "category")
    @Query("select p from Product p")
    List<Product> findAllWithCategory();

    @EntityGraph(attributePaths = "category")
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Integer id);

    @EntityGraph(attributePaths = "category")
    Page<Product> findBy(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    List<Product> findTop8ByOrderByCreateDateDesc();

    @EntityGraph(attributePaths = "category")
    List<Product> findTop8ByDiscountGreaterThanOrderByDiscountDesc(BigDecimal discount);

    @EntityGraph(attributePaths = "category")
    List<Product> findByCategoryId(String categoryId);

    @EntityGraph(attributePaths = "category")
    List<Product> findTop4ByCategoryIdAndIdNot(String categoryId, Integer id);

    @Query("""
            select p
            from Product p
            join p.orderDetails od
            join fetch p.category c
            group by p, c.id, c.name
            order by sum(od.quantity) desc
            """)
    List<Product> findBestSeller(Pageable pageable);

    @Query("""
            select p
            from Product p
            join fetch p.category c
            where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%')))
              and (:categoryId is null or :categoryId = '' or c.id = :categoryId)
              and (:minPrice is null or p.price >= :minPrice)
              and (:maxPrice is null or p.price <= :maxPrice)
            """)
    List<Product> search(@Param("keyword") String keyword,
                         @Param("categoryId") String categoryId,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice);

    @Query(value = """
            select p
            from Product p
            join fetch p.category c
            where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%')))
              and (:categoryId is null or :categoryId = '' or c.id = :categoryId)
              and (:minPrice is null or p.price >= :minPrice)
              and (:maxPrice is null or p.price <= :maxPrice)
            """, countQuery = """
            select count(p)
            from Product p
            join p.category c
            where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%')))
              and (:categoryId is null or :categoryId = '' or c.id = :categoryId)
              and (:minPrice is null or p.price >= :minPrice)
              and (:maxPrice is null or p.price <= :maxPrice)
            """)
    Page<Product> searchPage(@Param("keyword") String keyword,
                             @Param("categoryId") String categoryId,
                             @Param("minPrice") BigDecimal minPrice,
                             @Param("maxPrice") BigDecimal maxPrice,
                             Pageable pageable);

    @Query("""
            select p
            from Product p
            join p.category c
            where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%')))
              and (:categoryId is null or :categoryId = '' or c.id = :categoryId)
              and (:minPrice is null or p.price >= :minPrice)
              and (:maxPrice is null or p.price <= :maxPrice)
            order by p.price asc
            """)
    List<Product> searchOrderByPriceAsc(@Param("keyword") String keyword,
                                        @Param("categoryId") String categoryId,
                                        @Param("minPrice") BigDecimal minPrice,
                                        @Param("maxPrice") BigDecimal maxPrice);

    @Query("""
            select p
            from Product p
            join p.category c
            where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(c.name) like lower(concat('%', :keyword, '%')))
              and (:categoryId is null or :categoryId = '' or c.id = :categoryId)
              and (:minPrice is null or p.price >= :minPrice)
              and (:maxPrice is null or p.price <= :maxPrice)
            order by p.price desc
            """)
    List<Product> searchOrderByPriceDesc(@Param("keyword") String keyword,
                                         @Param("categoryId") String categoryId,
                                         @Param("minPrice") BigDecimal minPrice,
                                         @Param("maxPrice") BigDecimal maxPrice);
}
