package com.project.foret.service;

import com.project.foret.entity.*;
import com.project.foret.model.BoardModel;
import com.project.foret.model.MemberModel;
import com.project.foret.model.PhotoModel;
import com.project.foret.repository.*;
import com.project.foret.response.BoardResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@AllArgsConstructor
public class BoardService {

    @Autowired
    ServletContext servletContext;

    private BoardRepository boardRepository;
    private BoardPhotoRepository boardPhotoRepository;
    private MemberRepository memberRepository;
    private ForetRepository foretRepository;
    private CommentRepository commentRepository;

    private MemberService memberService;
    private CommentService commentService;

    public ResponseEntity<Object> createBoard(Long member_id, Long foret_id, Board board, MultipartFile[] files) throws Exception {
        HashMap map = new HashMap<String, String>();
        Board newBoard = new Board();
        newBoard.setType(board.getType());
        newBoard.setSubject(board.getSubject());
        newBoard.setContent(board.getContent());
        if (files != null) {
            for (MultipartFile photo : files) {
                newBoard.addPhoto(uploadPhoto(photo));
            }
        }
        if (member_id != null) {
            newBoard.setMember(new Member(member_id));
        }
        if (foret_id != null) {
            newBoard.setForet(new Foret(foret_id));
        }
        Board savedBoard = boardRepository.save(newBoard);
        if (boardRepository.findById(savedBoard.getId()).isPresent()) {
            map.put("result", "게시글 생성 성공");
            map.put("id", savedBoard.getId());
            return ResponseEntity.ok(map);
        } else {
            map.put("result", "게시글 생성 실패");
            return ResponseEntity.unprocessableEntity().body(map);
        }
    }

    @Transactional
    public ResponseEntity<Object> updateBoard(Long id, Long member_id, Board board, MultipartFile[] files) throws Exception {
        if (boardRepository.findById(id).isPresent()) {
            Board updateBoard = boardRepository.findById(id).get();
            if (updateBoard.getMember().getId().equals(member_id)) {
                boardPhotoRepository.deleteByBoardId(id);

                updateBoard.setType(board.getType());
                updateBoard.setSubject(board.getSubject());
                updateBoard.setContent(board.getContent());
                updateBoard.setEdit_date(Timestamp.valueOf(LocalDateTime.now()));
                if (files != null) {
                    for (MultipartFile photo : files) {
                        updateBoard.addPhoto(uploadPhoto(photo));
                    }
                }
                Board savedBoard = boardRepository.save(updateBoard);
                if (boardRepository.findById(savedBoard.getId()).isPresent()) {
                    return ResponseEntity.ok("게시글수정 성공");
                } else return ResponseEntity.unprocessableEntity().body("게시글수정 실패");
            } else return ResponseEntity.unprocessableEntity().body("게시글 수정권한이 없습니다.");
        } else return ResponseEntity.unprocessableEntity().body("게시글을 찾을 수 없습니다.");
    }

    public ResponseEntity<Object> deleteBoard(Long id) {
        if (boardRepository.findById(id).isPresent()) {
            boardRepository.deleteById(id);
            if (boardRepository.findById(id).isPresent())
                return ResponseEntity.unprocessableEntity().body("게시글삭제 실패");
            else return ResponseEntity.ok().body("게시글삭제 성공");
        } else return ResponseEntity.badRequest().body("게시글을 찾을 수 없습니다.");
    }

    public BoardModel getBoard(Long id) {
        if (boardRepository.findById(id).isPresent()) {
            Board board = boardRepository.findById(id).get();
            BoardModel boardModel = new BoardModel();
            boardModel.setId(board.getId());
            if(board.getForet() != null){
                boardModel.setForet_id(board.getForet().getId());
            } else {
                boardModel.setForet_id(null);
            }
            boardModel.setSubject(board.getSubject());
            boardModel.setContent(board.getContent());
            boardModel.setType(board.getType());
            boardModel.setHit(board.getHit());
            boardModel.setReg_date(board.getReg_date());
            boardModel.setEdit_date(board.getEdit_date());
            boardModel.setPhotos(getPhotoList(board));
            boardModel.setMember(getMember(board));
            boardModel.setComments(commentService.getComments(id));
            return boardModel;
        } else return null;
    }

    public BoardResponse getForetBoardList(Long foret_id) {
        if (foretRepository.findById(foret_id).isPresent()) {
            List<Board> boardList = boardRepository.findTop5ByForetIdOrderByIdDesc(foret_id);
            if (boardList.size() > 0) {
                List<BoardModel> boardModels = new ArrayList<>();
                for (Board board : boardList) {
                    BoardModel boardModel = new BoardModel();
                    boardModel.setId(board.getId());
                    boardModel.setForet_id(board.getForet().getId());
                    boardModel.setSubject(board.getSubject());
                    boardModel.setContent(board.getContent());
                    boardModel.setType(board.getType());
                    boardModel.setHit(board.getHit());
                    boardModel.setReg_date(board.getReg_date());
                    boardModel.setEdit_date(board.getEdit_date());
                    // boardModel.setPhotos(getPhotoList(board));
                    boardModel.setMember(getMember(board));
                    boardModels.add(boardModel);
                }
                return new BoardResponse(boardModels);
            } else return new BoardResponse();
        } else return new BoardResponse();
    }

    public BoardResponse getAnonymousBoardList(int order) {
        List<Board> boardList = new ArrayList<>();
        if (order == 1) {
            boardList = boardRepository.findByTypeOrderById(4);
        } else if (order == 2) {
            boardList = boardRepository.findByTypeOrderByCommentCount(4);
        }
        if (boardList.size() > 0) {
            List<BoardModel> boardModels = new ArrayList<>();
            for (Board board : boardList) {
                BoardModel boardModel = new BoardModel();
                boardModel.setId(board.getId());
//                boardModel.setForet_id(board.getForet().getId());
                boardModel.setSubject(board.getSubject());
                boardModel.setContent(board.getContent());
                boardModel.setType(board.getType());
                boardModel.setHit(board.getHit());
                boardModel.setReg_date(board.getReg_date());
                boardModel.setEdit_date(board.getEdit_date());
                // boardModel.setPhotos(getPhotoList(board));
                boardModel.setMember(getMember(board));
                boardModel.setComment_count(getCommentCount(board));
                boardModels.add(boardModel);
            }
            return new BoardResponse(boardModels);
        } else return new BoardResponse();
    }

    private List<PhotoModel> getPhotoList(Board board) {
        List<PhotoModel> photoList = new ArrayList<>();
        if (board.getPhotos() != null && board.getPhotos().size() != 0) {
            for (BoardPhoto boardPhoto : board.getPhotos()) {
                PhotoModel photoModel = new PhotoModel();
                photoModel.setId(boardPhoto.getId());
                photoModel.setDir(boardPhoto.getDir());
                photoModel.setFilename(boardPhoto.getFilename());
                photoModel.setOriginname(boardPhoto.getOriginname());
                photoModel.setFilesize(boardPhoto.getFilesize());
                photoModel.setFiletype(boardPhoto.getFiletype());
                photoModel.setReg_date(boardPhoto.getReg_date());
                photoList.add(photoModel);
            }
            return photoList;
        } else return null;
    }

    private BoardPhoto uploadPhoto(MultipartFile photo) throws Exception {
        String realPath = servletContext.getRealPath("/storage/board");
        String dir = "/storage/board";
        String originname = photo.getOriginalFilename();
        String filename = photo.getOriginalFilename();
        int lastIndex = originname.lastIndexOf(".");
        String filetype = originname.substring(lastIndex + 1);
        int filesize = (int) photo.getSize();
        if (!new File(realPath).exists()) {
            new File(realPath).mkdirs();
        }
        File file = new File(realPath, filename);
        FileCopyUtils.copy(photo.getInputStream(), new FileOutputStream(file));
        BoardPhoto boardPhoto = new BoardPhoto();
        boardPhoto.setDir(dir);
        boardPhoto.setOriginname(originname);
        boardPhoto.setFilename(filename);
        boardPhoto.setFiletype(filetype);
        boardPhoto.setFilesize(filesize);
        return boardPhoto;
    }

    private MemberModel getMember(Board board) {
        if (memberRepository.findById(board.getMember().getId()).isPresent()) {
            return memberService.getMember(board.getMember().getId());
        }
        return null;
    }

    private int getCommentCount(Board board) {
        if (board.getComments() != null && board.getComments().size() != 0) {
            return board.getComments().size();
        } else return 0;
    }
}
