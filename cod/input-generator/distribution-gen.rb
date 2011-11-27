$ngenes = ARGV[0].to_i
$max_predictors = ARGV[1].to_i
$target_gene = ARGV[2].to_i

def sum_hash(h)
  h.values.inject(&:+)
end

def format_binary(n)
  "%0#{$ngenes}b" % n
end

File.open("input.txt", "w") do |f|
  f.puts [$max_predictors, $target_gene].join(",")

  distribution = (0...2**$ngenes).inject({}) { |h, n| h[[format_binary(n), format_binary(rand(2**$ngenes))]] = rand(2**$ngenes); h }
  sum = sum_hash(distribution)
  distribution.each { |k, v| distribution[k] = v.to_f / sum }
  distribution.each do |k, v|
    distribution[k] += 1 - sum_hash(distribution)
    break
  end
  diff = (1 - sum_hash(distribution)).abs
  puts "warning, #{diff} difference to 1" if diff != 0
  distribution.each do |k, v|
    f.puts (k.map { |x| x.split "" }.flatten << v).join(",")
  end
end
