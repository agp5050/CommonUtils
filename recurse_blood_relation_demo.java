@Bean
class Node {
	private Integer id;
	private String type;
	private String nameEn;
	private String realType;
	private String connServer;
	private String ownerPath;
	private String manager;
	private List<Node> upNodes;
	private List<Node> downNodes;
  }
  @Bean
  class BloodParam {
	private Integer upLevel = 5;
	private Integer downLevel = 5;
	private Integer rowNum = 10;

	public Integer getUpLevel() {
		return upLevel;
	}

	public void setUpLevel(Integer upLevel) {
		this.upLevel = upLevel;
	}

	public Integer getDownLevel() {
		return downLevel;
	}

	public void setDownLevel(Integer downLevel) {
		this.downLevel = downLevel;
	}

	public Integer getRowNum() {
		return rowNum;
	}

	public void setRowNum(Integer rowNum) {
		this.rowNum = rowNum;
	}

}




//这个方法追溯job和scame的血缘关系，深度然后广度遍历。 最后一层遍历为0时终止，然后+1返回上一层。上一层由于parm的为1 只能执行一次
//最终这个方法最多有111110  个父类和间接父类。
	public void selectUp(Node node, BloodParam param) {
		param.setUpLevel(param.getUpLevel()-1); 
		if (param.getUpLevel()>= 0) {
			if (Global.SCHEMA.equals(node.getType())) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("flag", "up");
				params.put("id", node.getId());
				List<Node> upNodes = schemaDao.getJobNode(params);
				upNodes = getSubNodes(param, upNodes);
				node.setUpNodes(upNodes);
				for (int i=0; i<upNodes.size(); i++) {
					selectUp(upNodes.get(i), param);
				}
			} else if (Global.JOB.equals(node.getType())) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("flag", "up");
				params.put("id", node.getId());
				List<Node> upNodes = jobDao.getSchemaNode(params);
				upNodes = getSubNodes(param, upNodes);
				node.setUpNodes(upNodes);
				for (int i=0; i<upNodes.size(); i++) {
					selectUp(upNodes.get(i), param);
				}
			}
		}
		param.setUpLevel(param.getUpLevel()+1); 
	}
  
  
