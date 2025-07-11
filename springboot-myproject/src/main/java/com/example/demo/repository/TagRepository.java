package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer>{
	
	@Query(value = "select tag_id, tag_name from tag where tag_name = :name", nativeQuery = true)
	Optional<Tag> getTag(String name);
	
	 // 模糊搜尋（不區分大小寫）
    List<Tag> findByNameContainingIgnoreCase(String keyword);
    
    // 檢查tag的作品數
    @Query("SELECT COUNT(a) FROM Artwork a JOIN a.tags t WHERE t.id = :tagId")
    int countArtworkByTagId(@Param("tagId") Integer tagId);
}
