package com.oracle.gdms.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;
import com.oracle.gdms.dao.GoodsDao;
import com.oracle.gdms.entity.GoodsEntity;
import com.oracle.gdms.entity.GoodsModel;
import com.oracle.gdms.entity.GoodsTypeEntity;
import com.oracle.gdms.entity.PageModel;
import com.oracle.gdms.entity.ResponseEntity;
import com.oracle.gdms.service.GoodsService;
import com.oracle.gdms.util.GDMSUtil;
import com.oracle.gdms.web.listener.AppListener;

public class GoodsServiceImpl extends BaseService implements GoodsService {

	@Override
	public PageModel<GoodsModel> findByPage(int pageNumber, int rows) {
		GDMSUtil.log("商品分页查询显示调用开始……");
		PageModel<GoodsModel> obj = new PageModel<GoodsModel>();
		obj.setCurrent(pageNumber); // 当前页
		
		try {
			session = GDMSUtil.getSession();
			GoodsDao dao = session.getMapper(GoodsDao.class);
			
			int count = dao.findCount(null);	// 查询总记录行数
			int total = count % rows == 0 ? count / rows : count / rows + 1;	// 计算总页数
			obj.setTotal(total); // 总页数
			
			int offset = (pageNumber - 1) * rows;
			
			Map<String, Object> map = new HashMap<>();
			map.put("offset", offset);
			map.put("rows", rows);
			obj.setData( dao.findByPage(map) );  // 数据集也查出来
			GDMSUtil.log("商品分页查询显示调用结束");
		} catch (Exception e) {
			e.printStackTrace();
			GDMSUtil.log("商品分页查询显示调用时发生异常：" + e.toString());
		} finally {
			free();
		}
		 
		return obj;
	}

	@Override
	public String pushGoods(int goodsid) {
		try {
			session = GDMSUtil.getSession();
			GoodsDao dao = session.getMapper(GoodsDao.class);
			GoodsModel goods = dao.findById(goodsid);
			JSONObject json = new JSONObject();
			json.put("goods", goods);
			String jsonstr = json.toJSONString();
			
			ResponseEntity result = push(jsonstr); // 执行推送,结果保存到对象result中
			if ( result != null && result.getCode() == 0 ) {
				dao.updatePush(goodsid);	// 如果推送成功，就更新数据表字段push为已推送
				session.commit();
			}
			return result.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			free();
		}
		return "推送失败";
	}

	private ResponseEntity push(String jsonstr) {
		String url = AppListener.getString("pushurl_self");
		
		HttpPost post = new HttpPost(url);
		StringEntity entity = new StringEntity(jsonstr, "UTF-8");
		entity.setContentType("application/json");
		post.setEntity(entity);
		HttpClient client = new DefaultHttpClient();
		
		try {
			HttpResponse resp = client.execute(post);
			HttpEntity resent = resp.getEntity();
			
			String str = EntityUtils.toString(resent, "UTF-8");
			
			ResponseEntity re = JSONObject.parseObject(str, ResponseEntity.class);
			return re;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int add(GoodsEntity goods) {
		try {
			session = GDMSUtil.getSession();
			GoodsDao dao = session.getMapper(GoodsDao.class);
			int c = dao.add(goods);
			session.commit();
			return c;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			free();
		}
		return 0;
	}

	@Override
	public PageModel<GoodsModel> findByKeywords(String kw, int pageNumber, int rows) {
		PageModel<GoodsModel> obj = new PageModel<GoodsModel>();
		obj.setCurrent(pageNumber); // 当前页
		try {
			session = GDMSUtil.getSession();
			GoodsDao dao = session.getMapper(GoodsDao.class);
			
			int offset = (pageNumber - 1) * rows;
			Map<String, Object> map = new HashMap<>();
			map.put("offset", offset);
			map.put("rows", rows);
			map.put("kw", kw);
			int count = dao.findCount(map);	// 查询总记录行数
			int total = count % rows == 0 ? count / rows : count / rows + 1;	// 计算总页数
			obj.setTotal(total); // 总页数
			
			obj.setData( dao.findByPage(map) );  // 数据集也查出来
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			free();
		}
		 
		return obj;
	}

	@Override
	public List<GoodsModel> findByKeywords(String kw) {
		try {
			session = GDMSUtil.getSession();
			GoodsDao dao = session.getMapper(GoodsDao.class);
			return dao.findByKeywords(kw);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			free();
		}
		return null;
	}

	@Override
	public void delete(String[] gid) {
		try {
			session = GDMSUtil.getSession();
			GoodsDao dao = session.getMapper(GoodsDao.class);
			
			Map<String, Object> m = new HashMap<>();
			m.put("gid", gid);
			dao.updateStatus(m);
			
			session.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			free();
		}
	}

	@Override
	public int update(GoodsEntity goods) {
		// TODO 更新商品
		try {
			session = GDMSUtil.getSession();
			GoodsDao dao = session.getMapper(GoodsDao.class);
			dao.update(goods);
			session.commit();
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			free();
		}
		return 0;
	}

	@Override
	public List<GoodsTypeEntity> findAllType() {
		// TODO 查询所有商品类别
		List<GoodsTypeEntity> list = null;
		try {
			session = GDMSUtil.getSession();
			GoodsDao dao = session.getMapper(GoodsDao.class);
			list = dao.findAllType();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			free();
		}
		return list;
	}

	@Override
	public int updateGoodsType(GoodsEntity goods) {
		// TODO 自动生成的方法存根
		try {
			session = GDMSUtil.getSession();
			GoodsDao dao = session.getMapper(GoodsDao.class);
			dao.update(goods);
			session.commit();
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			free();
		}
		return 0;
	}

	@Override
	public List<GoodsModel> findByType(Integer gtid) {
		// TODO 根据类别查询商品
		List<GoodsModel> list = null;
		try {
			session = GDMSUtil.getSession();
			GoodsDao dao = session.getMapper(GoodsDao.class);
			list = dao.findByType(gtid);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			free();
		}
		return list;
	}
	
//	public static void main(String[] args) {
//		GoodsService ser = new GoodsServiceImpl();
//		PageModel<GoodsModel> p = ser.findByPage(1, 2);
//		System.out.println("总页数=" + p.getTotal());
//		for ( GoodsModel m : p.getData() ) {
//			System.out.print("goodsid=" + m.getGoodsid() + "  name=" + m.getName() );
//			System.out.println("  类别=" + m.getType().getName());
//		}
//	}

}
