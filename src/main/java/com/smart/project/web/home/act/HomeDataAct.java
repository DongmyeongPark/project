package com.smart.project.web.home.act;


import com.smart.project.proc.Test;
import com.smart.project.web.home.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;

import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Struct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@SessionAttributes("pageNum")
@Slf4j
@RestController
@RequiredArgsConstructor
public class HomeDataAct {
	final private Test test;


	@PostMapping("/data/mango2All")
	public Map<String, Object> getMango2DataAll(@RequestBody Map param){
		Map<String, Object> result = new HashMap<>();
		List<MangoVO> data = test.selectMango2All();
		/*List<mango2VO> data=null;*/
		log.error("select 결과 list : {}",data);
		result.put("food",data);

		return result;
	}
	// 검색 입력 or 음식 메뉴 클릭 시 음식점 리스트 띄우기
	@PostMapping("/data/searchAll")
	public Map<String, Object> getSearchAll(Model model ,@RequestBody Map param, HttpSession session, Criteria cri){
		Map<String, Object> result = new HashMap<>();
		String search = "";
		int pageNum = 1;
		if(param.get("search") != null)
			search = String.valueOf(param.get("search"));
		if(param.get("pageNum") != null )
			pageNum = Integer.parseInt(String.valueOf(param.get("pageNum")));
		model.addAttribute("pageNum",pageNum);
		cri.setSearch(search); // 검색 창에 입력한 것
		cri.setPage(pageNum); // 페이지 번호  1번누르면 1번 set
		for(MangoVO vo :test.searchAll(cri))
		{
			String placename = vo.getName();
			test.reviewAvg(placename);
			test.rvShow(placename);
		}
		List<MangoVO> data = test.searchAll(cri);

		int totalCount = test.totalCount(cri);
		if(!(totalCount==0))
		{
			PageMaker pageMaker = new PageMaker();
			pageMaker.setCri(cri);
			pageMaker.setTotalCount(totalCount);

			result.put("page",pageMaker);
		}
		else
		{
			result.put("page",null);
		}

		result.put("food",data);

		return result;
	}


	@PostMapping("/data/mango2")
	public List<MangoVO>getMango2Data(@RequestBody Map param, Criteria cri, Model model){
	 String mainmenu = String.valueOf(param.get("menu"));
	 String search = String.valueOf(param.get("menu"));

	 cri.setSearch(search);

		if(search!=null)
		{
/*
			List<mangoVO> list =test.getList(cri);// 해당 페이지의 perPageNum 만큼의 리스트
			model.addAttribute("list", list); // list --> ArrayList를 가르킨다.
			log.error("{}",list);*/
			/*
	//페이징 처리에 필요한 객체를 생성
			//1 2 3 4 5 6 7 8 9 10 페이지를 만들기위해서는 endpage, totalCount 등 PageMaker에 대한 객체생성
			PageMaker pageMaker= new PageMaker();
			//전체 게시물의 수를 구하기

			pageMaker.setCri(cri);   // 현재페이지를 넣어주자.
			pageMaker.setTotalCount(test.totalCount());

			model.addAttribute("pageMaker",pageMaker);*/
		}



	 log.error("검색창에 입력한 것 : {}",mainmenu);
	 List<MangoVO> data = test.selectMango2(mainmenu);
		/*List<mango2VO> data=null;*/
	log.error("select 결과 list : {}",data);
		return data;
	}

	@PostMapping("/data/map")
	public List<MangoVO>getMapData(@RequestBody Map param){
		String name = String.valueOf(param.get("name"));
		log.error("클릭한 맛집  : {}",name);
		test.viewCount(name);
		List<MangoVO> data = test.selectName(name);
		return data;
	}
	//위시리스트 값 가져와서 저장하기
	@RequestMapping("/wishStore")
	public WishListVO getData(@ModelAttribute WishListVO vo, HttpServletRequest request){
		String useremail = (String) request.getSession().getAttribute("email");
//		String placename = vo.getPlacename();
//		String roadname = vo.getRoadname();
//		String mainimg = vo.getMainimg();
//		log.error("세션에서 가져온 placename => {}",placename);
//		log.error("세션에서 가져온 roadname => {}",roadname);
//		log.error("세션에서 가져온 이미지src => {}",mainimg);
		vo.setUseremail(useremail);
		WishListVO data = vo;
			test.insertWish(vo);
		return data;
	}
	//위시리스트에 DB저장된 값 출력
	@RequestMapping("/data/wishSelect")
	public List<WishListVO> wishSelect(HttpServletRequest request){
		String useremail = (String)request.getSession().getAttribute("email");
		List<WishListVO> data = test.selectWish(useremail);
		return data;
	}
	//위시리스트에 선택한 리스트 삭제
	@RequestMapping("data/wishDelete")
	public WishListVO wishDelete(@RequestBody Map param, HttpServletRequest request){
		WishListVO vo = new WishListVO();
		String useremail = (String)request.getSession().getAttribute("email");
		String placename = (String)param.get("placeName");
		log.error("가져온 이메일 => {}",useremail);
		log.error("가져온 장소 => {}",placename);
		vo.setUseremail(useremail);
		vo.setPlacename(placename);
		test.wishDelete(vo);
		WishListVO data = vo;
		log.error("지운 data => {}",data);

		return data;
	}
	//위시리스트 확인 후 있으면 리턴받아 별표 색 유지
	@RequestMapping("data/haveWish")
	public WishListVO haveWish(@RequestBody Map param,HttpServletRequest request){
		WishListVO vo = new WishListVO();
		String useremail = (String)request.getSession().getAttribute("email");
		String placename = (String)param.get("placeName");
		vo.setUseremail(useremail);
		vo.setPlacename(placename);
		WishListVO data = test.haveWish(vo);
		return data;
	}

	//해당 이메일에 로그인되어있을 때 리뷰 삭제
	@RequestMapping("data/deleteReply")
	public void deleteReply(@ModelAttribute ReviewDTO dto) {
		String email =  dto.getEmail();
		String title = dto.getTitle();
		dto.setEmail(email);
		log.error("지울 것? => {}",dto.getEmail());
		ReviewDTO data = dto;

		test.reviewCount(title, -1);
		test.deleteReply(email);
	}

	@PostMapping("/idCheck")
	public int checkDuplicateId(@RequestBody Map param){
		String id = String.valueOf(param.get("userId"));
		int idCount = test.idCount(id);

		return idCount;
	}
	@PostMapping("/saveReview")
	public void saveReview(MultipartHttpServletRequest request) throws IOException {
		ModelAndView mav = new ModelAndView();
		MultipartHttpServletRequest multi = request;
		List<MultipartFile> file = multi.getFiles("file");
		String id = request.getParameter("email");
		String title = request.getParameter("title");
		int grade = Integer.parseInt(request.getParameter("grade"));
		String review = request.getParameter("review");
		ReviewDTO reviewDTO = new ReviewDTO();
		reviewDTO.setEmail(id);
		reviewDTO.setTitle(title);
		reviewDTO.setGrade(grade);
		reviewDTO.setReview(review);
		try {
			byte[] img = file.get(0).getBytes();
			reviewDTO.setImg(img);
		} catch (IOException e){
			e.printStackTrace();
		}
		Iterator itr = request.getFileNames();
		List<MultipartFile> file_list = request.getFiles( (String) itr.next());
		if( file_list.size() > 0 ){
			for( MultipartFile mpf : file_list ){
				if( ! mpf.isEmpty() ){    // 파일이 빈 껍데기가 아닐때
					test.reviewCount(title, 1);
					test.saveReview(reviewDTO);
					// 파일 저장이든 DB에 값넣고 insert를 하던지 하기
				}
			}
		}
	}


	@RequestMapping("/getReview")
	public ReviewDTO getReview(String reviewId) {
		log.error("{}===>",reviewId+"reviewId");
		return test.getReview(reviewId);
	}
	@RequestMapping("/getReviewsByKeySet")
	public List<ReviewDTO> getReviewsByKeySet(String reviewUpdateDate, String reviewId) {
		log.error("{}===>",reviewUpdateDate,reviewId+"reviewUpdateDate,reviewId");
		return test.getReviewsByKeySet(reviewUpdateDate, reviewId);
	}
	@RequestMapping("/getReviewsForMap")
	public List<ReviewDTO> getReviewsForMap() {
		return test.getReviewsForMap();
	}

	@RequestMapping("/deleteReviews")
	public void deleteReviews(ReviewDTO reviewDTO) {
		List<String> reviewIds = reviewDTO.getReviewIds();
		log.error("{}===>",reviewIds+"reviewIds");
		test.deleteReviews(reviewIds);
	}
	@GetMapping("/getImages")
	public List<FileDTO> getImages(@RequestParam String reviewId) {
		log.error("{}===>",reviewId+"reviewId+asdfasf");

		return test.getImages(reviewId);
	}
	private Path imgDirPath;

	@Value("src/main/resources/static/upload-dir")
	private String imgDir;

	@PostConstruct
	public void init() {
		this.imgDirPath = Paths.get(imgDir);
	}


	@RequestMapping("/delete")
	public void deleteFiles(ReviewDTO reviewDTO) {
		List<String> fileIds = reviewDTO.getFileIds();
		if (fileIds == null || fileIds.isEmpty()) {
			return;
		}
		test.deleteFiles(fileIds);
	}
	@RequestMapping(value = "/multipartUpload.do", method = RequestMethod.POST)
	public String uploadSingle(@RequestParam("files") MultipartFile file) throws Exception {
		String rootPath = FileSystemView.getFileSystemView().getHomeDirectory().toString();
		String basePath = rootPath + "/" + "single";
		String filePath = basePath + "/" + file.getOriginalFilename();
		File dest = new File(filePath);
		log.error("가져온 파일 => {}",rootPath);
		log.error("가져온 파일 => {}",basePath);
		log.error("가져온 파일 => {}",filePath);
//		test.transferTo(dest); // 파일 업로드 작업 수행
		return "uploaded";
	}
	@RequestMapping("/get")
	public void convertBinary(MultipartFile files) throws IOException {
//		String fileName = StringUtils.cleanPath(files.getOriginalFilename()) ;
//		BufferedImage image = ImageIO.read(files.getInputStream());
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		ImageIO.write(image, fileName.substring(fileName.lastIndexOf(".") + 1), baos);
//		log.error("가져온 파일 => {}",fileName);
//		log.error("가져온 image => {}",image);
//		log.error("가져온 baos => {}",baos);
		/*List<MultipartFile> file = (List<MultipartFile>) files.getFile("files");
		log.error("가져온 파일 => {}",files);
		String reviewId = reviewDTO.getEmail();*/


//
//			if (! Files.exists(imgDirPath)) {
//				Files.createDirectories(imgDirPath);
//			}
//
//			Path reviewImgDirPath = imgDirPath.resolve(
//					Paths.get(reviewId)).normalize().toAbsolutePath();
//
//			if (! Files.exists(reviewImgDirPath)) {
//				Files.createDirectories(reviewImgDirPath);
//			}
//
//		for (MultipartFile file : files) {
//			saveFile(file, reviewId);
//		}
	}

	@RequestMapping("/save")
	public void saveFile(MultipartFile file, String reviewId) {
		try {
			String originFilename = file.getOriginalFilename();
			long fileSize = file.getSize();
			String contentType = file.getContentType();


			Path destinationFile = imgDirPath
					.resolve(Paths.get(reviewId))
					.resolve(Paths.get(originFilename))
					.normalize()
					.toAbsolutePath();

			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
			}

			String fileId = UUID.randomUUID().toString();
			test.saveFile(fileId, reviewId, originFilename, fileSize, contentType);
		} catch (IOException e) {
			log.error("filenotfond",e);
		}
	}

}
